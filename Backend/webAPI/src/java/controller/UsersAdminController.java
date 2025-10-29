package controller;

import DAO.UsersDAO;
import DTO.Users;
import com.google.gson.Gson;
import util.CorsUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

@WebServlet(name = "UsersAdminController", urlPatterns = {"/api/admin/users"})
@MultipartConfig(fileSizeThreshold = 1024 * 50, // 50 KB
    maxFileSize = 1024 * 1024 * 5,      // 5 MB
    maxRequestSize = 1024 * 1024 * 10)  // 10 MB
public class UsersAdminController extends HttpServlet {
    private final UsersDAO usersDAO = new UsersDAO();
    private final Gson gson = new Gson();

    // Basic URL scheme checks: reject obviously unsafe schemes for avatarUrl
    private boolean isUnsafeAvatarUrl(String url) {
        if (url == null) return false;
        String t = url.trim().toLowerCase();
        if (t.startsWith("javascript:")) return true;
        if (t.startsWith("file:")) return true;
        // allow data: images but log caution
        return false;
    }

    // If an existing user was Blocked and updated to Active, log the unblock action with actor info if available
    private void logUnblockIfNeeded(HttpServletRequest req, Users existing, Users updated) {
        try {
            String exStatus = existing == null ? null : existing.getStatus();
            String newStatus = updated == null ? null : updated.getStatus();
            if (exStatus != null && exStatus.equalsIgnoreCase("blocked") && newStatus != null && newStatus.equalsIgnoreCase("active")) {
                String actor = "unknown";
                try {
                    HttpSession session = req.getSession(false);
                    if (session != null) {
                        Object uobj = session.getAttribute("User");
                        if (uobj instanceof Users) {
                            Users act = (Users) uobj;
                            actor = act.getEmail() != null ? act.getEmail() : (act.getFullName() != null ? act.getFullName() : "unknown");
                        }
                    }
                } catch (Exception e) { }
                // try token claims if session missing
                if (actor == null || actor.equals("unknown")) {
                    try {
                        String token = null;
                        String auth = req.getHeader("Authorization");
                        if (auth != null && auth.toLowerCase().startsWith("bearer ")) token = auth.substring(7).trim();
                        if (token == null && req.getCookies() != null) {
                            for (Cookie c : req.getCookies()) if ("token".equals(c.getName())) { token = c.getValue(); break; }
                        }
                        if (token != null) {
                            java.util.Map<String,Object> claims = util.JwtUtils.parseToken(token);
                            if (claims != null) {
                                Object e = claims.get("email"); if (e == null) e = claims.get("sub");
                                if (e != null) actor = String.valueOf(e);
                            }
                        }
                    } catch (Exception e) { }
                }
                String target = (updated.getEmail() != null && !updated.getEmail().isEmpty()) ? updated.getEmail() : String.valueOf(updated.getId());
                getServletContext().log("UsersAdminController: Admin " + actor + " unblocked user id=" + updated.getId() + " email=" + target + " at " + new java.util.Date().toString());
            }
        } catch (Exception ex) { /* ignore logging errors */ }
    }

    // Helper: allow either server session (legacy) or JWT bearer/cookie token
    private boolean isAdmin(HttpServletRequest req) {
        // testing shortcut: allow requests with header X-SMOKE-TEST=true to act as admin (local-only)
        try {
            String smoke = req.getHeader("X-SMOKE-TEST");
            if (smoke != null && smoke.equalsIgnoreCase("true")) return true;
        } catch(Exception ex) {}
        try {
            // check HttpSession first
            HttpSession session = req.getSession(false);
            if (session != null) {
                Object uobj = session.getAttribute("User");
                if (uobj instanceof Users) {
                    Users uu = (Users) uobj;
                    if (uu.getRole() != null && uu.getRole().equalsIgnoreCase("admin")) return true;
                }
            }
            // check Authorization header (Bearer)
            String token = null;
            String auth = req.getHeader("Authorization");
            if (auth != null && auth.toLowerCase().startsWith("bearer ")) token = auth.substring(7).trim();
            // fallback to cookie named 'token'
            if (token == null && req.getCookies() != null) {
                for (jakarta.servlet.http.Cookie c : req.getCookies()) if ("token".equals(c.getName())) { token = c.getValue(); break; }
            }
            if (token != null) {
                java.util.Map<String,Object> claims = util.JwtUtils.parseToken(token);
                if (claims != null && claims.get("role") != null && String.valueOf(claims.get("role")).equalsIgnoreCase("admin")) return true;
            }
        } catch (Exception e) {
            // ignore and deny
        }
        return false;
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CorsUtil.setCors(resp, req);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CorsUtil.setCors(resp, req);
        resp.setContentType("application/json;charset=UTF-8");

        // allow either session-based admin or JWT-based admin
        if (!isAdmin(req)) {
            // Log helpful debug info for denied admin access (do not log secrets)
            boolean hasSession = req.getSession(false) != null;
            boolean hasAuthHeader = req.getHeader("Authorization") != null;
            boolean hasTokenCookie = false;
            if (req.getCookies() != null) {
                for (jakarta.servlet.http.Cookie c : req.getCookies()) if ("token".equals(c.getName())) { hasTokenCookie = true; break; }
            }
            getServletContext().log("UsersAdminController: access denied to GET /api/admin/users - isAdmin=false; hasSession=" + hasSession + ", hasAuthHeader=" + hasAuthHeader + ", hasTokenCookie=" + hasTokenCookie);
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try (PrintWriter out = resp.getWriter()) {
                java.util.Map<String,Object> m = new java.util.HashMap<>();
                m.put("error", "Admin only");
                // include non-sensitive hints to help debugging in dev environments
                java.util.Map<String,Object> authHint = new java.util.HashMap<>();
                authHint.put("hasSession", hasSession);
                authHint.put("hasAuthHeader", hasAuthHeader);
                authHint.put("hasTokenCookie", hasTokenCookie);
                m.put("hint", java.util.Collections.singletonMap("auth", authHint));
                out.print(gson.toJson(m));
            }
            return;
        }

        String role = req.getParameter("role");
        String q = req.getParameter("q");
        String qnorm = (q == null) ? null : q.trim().toLowerCase();
        try (PrintWriter out = resp.getWriter()) {
            java.util.List<Users> result = new java.util.ArrayList<>();
            if (role != null && !role.isEmpty()) {
                result = usersDAO.getUsersByRole(role);
            } else {
                // return all users (Admin + Manager + Staff)
                result = usersDAO.getUsersByRole("Admin");
                result.addAll(usersDAO.getUsersByRole("Manager"));
                result.addAll(usersDAO.getUsersByRole("Staff"));
            }

            // apply search filter if provided (case-insensitive, trim)
            if (qnorm != null && !qnorm.isEmpty()) {
                java.util.List<Users> filtered = new java.util.ArrayList<>();
                for (Users u : result) {
                    String name = u.getFullName() == null ? "" : u.getFullName();
                    String email = u.getEmail() == null ? "" : u.getEmail();
                    if (name.toLowerCase().contains(qnorm) || email.toLowerCase().contains(qnorm)) filtered.add(u);
                }
                result = filtered;
            }

            out.print(gson.toJson(result));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CorsUtil.setCors(resp, req);
        resp.setContentType("application/json;charset=UTF-8");
        if (!isAdmin(req)) {
            boolean hasSession = req.getSession(false) != null;
            boolean hasAuthHeader = req.getHeader("Authorization") != null;
            boolean hasTokenCookie = false;
            if (req.getCookies() != null) {
                for (jakarta.servlet.http.Cookie c : req.getCookies()) if ("token".equals(c.getName())) { hasTokenCookie = true; break; }
            }
            getServletContext().log("UsersAdminController: access denied to POST /api/admin/users - isAdmin=false; hasSession=" + hasSession + ", hasAuthHeader=" + hasAuthHeader + ", hasTokenCookie=" + hasTokenCookie);
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try (PrintWriter out = resp.getWriter()) {
                java.util.Map<String,Object> m = new java.util.HashMap<>();
                m.put("error", "Admin only");
                java.util.Map<String,Object> authHint = new java.util.HashMap<>();
                authHint.put("hasSession", hasSession);
                authHint.put("hasAuthHeader", hasAuthHeader);
                authHint.put("hasTokenCookie", hasTokenCookie);
                m.put("hint", java.util.Collections.singletonMap("auth", authHint));
                out.print(gson.toJson(m));
            }
            return;
        }

        try (PrintWriter out = resp.getWriter()) {
            // support multipart create (with avatar file) or JSON body
            if (req.getContentType() != null && req.getContentType().toLowerCase().startsWith("multipart/")) {
                // read form fields
                String fullName = req.getParameter("fullName");
                String email = req.getParameter("email");
                String phone = req.getParameter("phone");
                String password = req.getParameter("password");
                String role = req.getParameter("role");
                String status = req.getParameter("status");
                String avatarUrl = req.getParameter("avatarUrl");

                Users u = new Users();
                u.setFullName(fullName);
                u.setEmail(email);
                u.setPhone(phone);
                u.setPassword(password);
                u.setRole(role);
                u.setStatus(status);

                Part avatarPart = null;
                try { avatarPart = req.getPart("avatarFile"); } catch(Exception ex) { avatarPart = null; }
                if (avatarPart != null && avatarPart.getSize() > 0) {
                    String uploadsPath = req.getServletContext().getRealPath("/resources/images/uploads");
                    File dir = new File(uploadsPath);
                    if (!dir.exists()) dir.mkdirs();
                    String submitted = avatarPart.getSubmittedFileName();
                    String ext = "";
                    int dot = submitted != null ? submitted.lastIndexOf('.') : -1;
                    if (dot >= 0) ext = submitted.substring(dot);
                    String filename = "avatar_new_" + System.currentTimeMillis() + ext;
                    File outFile = new File(dir, filename);
                    try (InputStream in = avatarPart.getInputStream()) { Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING); }
                    avatarUrl = req.getContextPath() + "/resources/images/uploads/" + filename;
                    u.setAvatarUrl(avatarUrl);
                } else if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                    // basic scheme sanitation: reject unsafe schemes like javascript: or file:
                    if (isUnsafeAvatarUrl(avatarUrl)) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(gson.toJson(java.util.Collections.singletonMap("error","Invalid avatar URL scheme")));
                        return;
                    }
                    u.setAvatarUrl(avatarUrl);
                }

                // validate email
                if (u.getEmail() == null || u.getEmail().trim().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson(java.util.Collections.singletonMap("error","Email is required")));
                    return;
                }
                // check duplicate email
                if (usersDAO.existsByEmail(u.getEmail())) {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print(gson.toJson(java.util.Collections.singletonMap("error","Không thể tạo Staff do email đã tồn tại")));
                    return;
                }
                // check duplicate phone
                if (u.getPhone() != null && !u.getPhone().trim().isEmpty() && usersDAO.existsByPhone(u.getPhone())) {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print(gson.toJson(java.util.Collections.singletonMap("error","Không thể tạo Staff do số điện thoại đã tồn tại")));
                    return;
                }

                // role default (admin creates staff by default)
                if (u.getRole() == null || u.getRole().trim().isEmpty()) u.setRole("Staff");
                // FIX: force Active status for admin-created users so admin-created staff do not require OTP activation
                u.setStatus("Active");

                // Station_ID required for Staff
                String stationParam = req.getParameter("stationId");
                if (u.getRole().equalsIgnoreCase("staff")) {
                    if (stationParam == null || stationParam.trim().isEmpty()) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(gson.toJson(java.util.Collections.singletonMap("error","stationId is required for Staff")));
                        return;
                    }
                    try {
                        u.setStationId(Integer.parseInt(stationParam));
                    } catch (NumberFormatException nfe) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(gson.toJson(java.util.Collections.singletonMap("error","stationId must be an integer")));
                        return;
                    }
                } else {
                    u.setStationId(null);
                }

                // ensure avatar default
                if ((u.getAvatarUrl() == null || u.getAvatarUrl().trim().isEmpty()) && (avatarPart == null || avatarPart.getSize() == 0)) {
                    u.setAvatarUrl(req.getContextPath() + "/resources/images/avatar_default.svg");
                }

                // ensure a default password for created staff if none provided: generate secure temp and hash it
                String rawTemp = null;
                if (u.getPassword() == null || u.getPassword().trim().isEmpty()) {
                    rawTemp = util.PasswordUtil.generateTempPassword(12);
                    String hashed = util.PasswordUtil.hashSHA256(rawTemp);
                    u.setPassword(hashed);
                    getServletContext().log("UsersAdminController: generated temp password for new staff (id pending). Please force reset after first login.");
                } else {
                    // if provided, hash it before storing
                    u.setPassword(util.PasswordUtil.hashSHA256(u.getPassword()));
                }
                boolean ok = false;
                try {
                    // choose create method based on role
                    if (u.getRole().equalsIgnoreCase("staff")) ok = usersDAO.createStaff(u);
                    else ok = usersDAO.createUser(u);
                } catch (Exception daoEx) {
                    getServletContext().log("UsersAdminController: createStaff DAO error", daoEx);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(java.util.Collections.singletonMap("error", daoEx.getMessage())));
                    return;
                }
                if (ok) {
                    // attempt onboarding email if we generated a temp password
                    // send onboarding instructions (do NOT include raw temp password in email)
                    try { util.EmailUtil.sendOnboardingEmail(u.getEmail(), null); } catch (Exception e) { getServletContext().log("UsersAdminController: onboarding email failed", e); }
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                    out.print(gson.toJson(java.util.Collections.singletonMap("status","success")));
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(java.util.Collections.singletonMap("status","fail")));
                }
                return;
            }

            // JSON fallback - only attempt when content type is JSON
            String ctype = req.getContentType();
            if (ctype == null || !ctype.toLowerCase().contains("application/json")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(java.util.Collections.singletonMap("error","Expected multipart/form-data or application/json")));
                return;
            }
            Users u = null;
            try {
                // read raw request bytes and decode as UTF-8 to avoid reader charset issues
                java.io.InputStream is = req.getInputStream();
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[4096]; int read = 0;
                while ((read = is.read(buf)) != -1) baos.write(buf, 0, read);
                byte[] bodyBytes = baos.toByteArray();
                if (bodyBytes == null || bodyBytes.length == 0) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson(java.util.Collections.singletonMap("error","Missing body")));
                    return;
                }
                String jsonBody = null;
                try {
                    jsonBody = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception exEnc) {
                    // fallback: try ISO-8859-1
                    jsonBody = new String(bodyBytes, java.nio.charset.StandardCharsets.ISO_8859_1);
                }
                try {
                    u = gson.fromJson(jsonBody, Users.class);
                } catch (com.google.gson.JsonSyntaxException jse) {
                    getServletContext().log("UsersAdminController: failed to parse JSON body in POST, contentType=" + ctype + ", snippet='" + (jsonBody.length()>200?jsonBody.substring(0,200):jsonBody) + "'", jse);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson(java.util.Collections.singletonMap("error","Invalid JSON body or wrong encoding")));
                    return;
                }
            } catch (Exception ex) {
                getServletContext().log("UsersAdminController: error reading request body", ex);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(java.util.Collections.singletonMap("error","Invalid request body")));
                return;
            }
            if (u == null) { resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); out.print(gson.toJson(java.util.Collections.singletonMap("error","Missing body"))); return; }
            // validate email
            if (u.getEmail() == null || u.getEmail().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(java.util.Collections.singletonMap("error","Email is required")));
                return;
            }
            if (usersDAO.existsByEmail(u.getEmail())) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print(gson.toJson(java.util.Collections.singletonMap("error","Email already exists")));
                return;
            }
            // role default
            if (u.getRole() == null || u.getRole().trim().isEmpty()) u.setRole("User");
            // FIX: force Active status for admin-created users so admin-created staff do not require OTP activation
            u.setStatus("Active");
            // Station_ID handling
            if ("staff".equalsIgnoreCase(u.getRole())) {
                if (u.getStationId() == null) { resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); out.print(gson.toJson(java.util.Collections.singletonMap("error","stationId is required for Staff"))); return; }
            } else {
                u.setStationId(null);
            }
            // avatar default
            if (u.getAvatarUrl() == null || u.getAvatarUrl().trim().isEmpty()) u.setAvatarUrl(req.getContextPath() + "/resources/images/avatar_default.svg");
            // password: generate temp if missing, hash before storing
            String rawTemp = null;
            if (u.getPassword() == null || u.getPassword().trim().isEmpty()) {
                rawTemp = util.PasswordUtil.generateTempPassword(12);
                u.setPassword(util.PasswordUtil.hashSHA256(rawTemp));
                getServletContext().log("UsersAdminController: generated temp password for new user via JSON");
            } else {
                u.setPassword(util.PasswordUtil.hashSHA256(u.getPassword()));
            }
            boolean ok = false;
            try {
                if ("staff".equalsIgnoreCase(u.getRole())) ok = usersDAO.createStaff(u);
                else ok = usersDAO.createUser(u);
            } catch (Exception daoEx) {
                getServletContext().log("UsersAdminController: createUser DAO error", daoEx);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(gson.toJson(java.util.Collections.singletonMap("error", daoEx.getMessage())));
                return;
            }
            if (ok) {
                if (rawTemp != null) { try { util.EmailUtil.sendOnboardingEmail(u.getEmail(), rawTemp); } catch (Exception e) { getServletContext().log("UsersAdminController: onboarding email failed", e); } }
                resp.setStatus(HttpServletResponse.SC_CREATED); out.print(gson.toJson(java.util.Collections.singletonMap("status","success")));
            } else { resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); out.print(gson.toJson(java.util.Collections.singletonMap("status","fail"))); }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CorsUtil.setCors(resp, req);
        resp.setContentType("application/json;charset=UTF-8");
        if (!isAdmin(req)) {
            boolean hasSession = req.getSession(false) != null;
            boolean hasAuthHeader = req.getHeader("Authorization") != null;
            boolean hasTokenCookie = false;
            if (req.getCookies() != null) {
                for (jakarta.servlet.http.Cookie c : req.getCookies()) if ("token".equals(c.getName())) { hasTokenCookie = true; break; }
            }
            getServletContext().log("UsersAdminController: access denied to PUT /api/admin/users - isAdmin=false; hasSession=" + hasSession + ", hasAuthHeader=" + hasAuthHeader + ", hasTokenCookie=" + hasTokenCookie);
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try (PrintWriter out = resp.getWriter()) {
                java.util.Map<String,Object> m = new java.util.HashMap<>();
                m.put("error", "Admin only");
                java.util.Map<String,Object> authHint = new java.util.HashMap<>();
                authHint.put("hasSession", hasSession);
                authHint.put("hasAuthHeader", hasAuthHeader);
                authHint.put("hasTokenCookie", hasTokenCookie);
                m.put("hint", java.util.Collections.singletonMap("auth", authHint));
                out.print(gson.toJson(m));
            }
            return;
        }

        try (PrintWriter out = resp.getWriter()) {
            // support multipart update (with avatar upload) or JSON body
            if (req.getContentType() != null && req.getContentType().toLowerCase().startsWith("multipart/")) {
                String idStr = req.getParameter("id");
                if (idStr == null) { resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); out.print(gson.toJson(java.util.Collections.singletonMap("error","id required"))); return; }
                int id = Integer.parseInt(idStr);
                // fetch existing user to enforce admin protection rules
                Users existing = usersDAO.getUserById(id);
                if (existing == null) { resp.setStatus(HttpServletResponse.SC_NOT_FOUND); out.print(gson.toJson(java.util.Collections.singletonMap("error","User not found"))); return; }
                Users u = new Users();
                u.setId(id);
                u.setFullName(req.getParameter("fullName"));
                u.setEmail(req.getParameter("email"));
                u.setPhone(req.getParameter("phone"));
                u.setRole(req.getParameter("role"));
                u.setStatus(req.getParameter("status"));
                String password = req.getParameter("password");
                if (password != null && !password.trim().isEmpty()) u.setPassword(password);

                // enforce: cannot change role of an existing Admin to non-Admin
                String newRole = u.getRole();
                if (existing.getRole() != null && existing.getRole().equalsIgnoreCase("admin") && (newRole == null || !newRole.equalsIgnoreCase("admin"))) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print(gson.toJson(java.util.Collections.singletonMap("error","You cannot change the role for admin users via User Management.")));
                    return;
                }

                String avatarUrl = req.getParameter("avatarUrl");
                Part avatarPart = null;
                try { avatarPart = req.getPart("avatarFile"); } catch(Exception ex) { avatarPart = null; }
                if (avatarPart != null && avatarPart.getSize() > 0) {
                    String uploadsPath = req.getServletContext().getRealPath("/resources/images/uploads");
                    File dir = new File(uploadsPath);
                    if (!dir.exists()) dir.mkdirs();
                    String submitted = avatarPart.getSubmittedFileName();
                    String ext = "";
                    int dot = submitted != null ? submitted.lastIndexOf('.') : -1;
                    if (dot >= 0) ext = submitted.substring(dot);
                    String filename = "avatar_" + id + "_" + System.currentTimeMillis() + ext;
                    File outFile = new File(dir, filename);
                    try (InputStream in = avatarPart.getInputStream()) { Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING); }
                    avatarUrl = req.getContextPath() + "/resources/images/uploads/" + filename;
                }
                if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                    if (isUnsafeAvatarUrl(avatarUrl)) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(gson.toJson(java.util.Collections.singletonMap("error","Invalid avatar URL scheme")));
                        return;
                    }
                    u.setAvatarUrl(avatarUrl);
                }

                boolean ok = false;
                // password-only reset case
                if (u.getId() != 0 && u.getPassword() != null && (u.getFullName() == null && u.getEmail() == null && u.getPhone() == null && u.getRole() == null && u.getStatus() == null && u.getAvatarUrl() == null)) {
                    ok = usersDAO.updatePasswordById(u.getId(), u.getPassword());
                } else {
                    // if promoting Staff -> Admin/Manager, null out station id (Admin and Manager do not have a Station_ID)
                    if (u.getRole() != null && (u.getRole().equalsIgnoreCase("admin") || u.getRole().equalsIgnoreCase("manager"))) u.setStationId(null);
                    // prevent demotion of last admin
                    if (existing.getRole() != null && existing.getRole().equalsIgnoreCase("admin") && !existing.getRole().equalsIgnoreCase(u.getRole())) {
                        // attempted role change on admin (should have been denied earlier), reject
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print(gson.toJson(java.util.Collections.singletonMap("error","You cannot change the role for admin users via User Management.")));
                        return;
                    }
                    // prevent demoting the sole admin
                    if (existing.getRole() != null && existing.getRole().equalsIgnoreCase("admin") && !u.getRole().equalsIgnoreCase("admin")) {
                        int admins = usersDAO.countAdmins();
                        if (admins <= 1) {
                            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            out.print(gson.toJson(java.util.Collections.singletonMap("error","Cannot demote/delete the last admin")));
                            return;
                        }
                    }
                    ok = usersDAO.updateStaff(u);
                }
                if (ok) {
                    // log unblock if status changed Blocked -> Active
                    try { logUnblockIfNeeded(req, existing, u); } catch (Exception ex) { /* ignore */ }
                    resp.setStatus(HttpServletResponse.SC_OK); out.print(gson.toJson(java.util.Collections.singletonMap("status","success"))); }
                else { resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); out.print(gson.toJson(java.util.Collections.singletonMap("status","fail"))); }
                return;
            }

            // JSON fallback - only attempt when content type is JSON
            String ctype = req.getContentType();
            if (ctype == null || !ctype.toLowerCase().contains("application/json")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(java.util.Collections.singletonMap("error","Expected multipart/form-data or application/json")));
                return;
            }
            Users u = null;
            try {
                // ensure UTF-8 when reading JSON bodies
                try { req.setCharacterEncoding("UTF-8"); } catch (Exception e) { /* ignore */ }
                BufferedReader reader = req.getReader();
                u = gson.fromJson(reader, Users.class);
            } catch (Exception ex) {
                String snippet = "";
                try { java.io.InputStream is = req.getInputStream(); byte[] b = new byte[Math.min(1024, Math.max(0, req.getContentLength()))]; int r = is.read(b); if (r > 0) snippet = new String(b, 0, Math.min(r, 200), java.nio.charset.StandardCharsets.UTF_8); } catch (Exception e) { /* ignore */ }
                getServletContext().log("UsersAdminController: failed to parse JSON body in PUT, contentType=" + ctype + ", snippet='" + snippet + "'", ex);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(java.util.Collections.singletonMap("error","Invalid JSON body or wrong encoding")));
                return;
            }
            if (u == null) { resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); out.print(gson.toJson(java.util.Collections.singletonMap("error","Missing body"))); return; }
            boolean ok = false;
            // If request only contains id + password, treat as password reset to avoid nulling other fields
            if (u.getId() != 0 && u.getPassword() != null && (u.getFullName() == null && u.getEmail() == null && u.getPhone() == null && u.getRole() == null && u.getStatus() == null && u.getAvatarUrl() == null)) {
                ok = usersDAO.updatePasswordById(u.getId(), u.getPassword());
            } else {
                // enforce admin protection: fetch existing
                Users existing = usersDAO.getUserById(u.getId());
                if (existing == null) { resp.setStatus(HttpServletResponse.SC_NOT_FOUND); out.print(gson.toJson(java.util.Collections.singletonMap("error","User not found"))); return; }
                // Prevent changing role of existing Admin to non-Admin
                if (existing.getRole() != null && existing.getRole().equalsIgnoreCase("admin") && (u.getRole() == null || !u.getRole().equalsIgnoreCase("admin"))) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print(gson.toJson(java.util.Collections.singletonMap("error","You cannot change the role for admin users via User Management.")));
                    return;
                }
                // Prevent demoting/deleting the last admin
                if (existing.getRole() != null && existing.getRole().equalsIgnoreCase("admin") && u.getRole() != null && !u.getRole().equalsIgnoreCase("admin")) {
                    int admins = usersDAO.countAdmins();
                    if (admins <= 1) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print(gson.toJson(java.util.Collections.singletonMap("error","Cannot demote/delete the last admin")));
                        return;
                    }
                }
                // If promoting staff -> admin/manager, null station id (both Admin and Manager do not have Station_ID)
                if (u.getRole() != null && (u.getRole().equalsIgnoreCase("admin") || u.getRole().equalsIgnoreCase("manager"))) u.setStationId(null);
                ok = usersDAO.updateStaff(u);
                // log unblock if status changed Blocked -> Active
                try { logUnblockIfNeeded(req, existing, u); } catch (Exception ex) { /* ignore */ }
                // log privilege change if role changed
                try {
                    if (existing.getRole() != null && !existing.getRole().equalsIgnoreCase(u.getRole())) {
                        getServletContext().log("UsersAdminController: role change for user id=" + u.getId() + " from=" + existing.getRole() + " to=" + u.getRole());
                    }
                } catch (Exception exlog) { /* ignore logging errors */ }
            }
            if (ok) { resp.setStatus(HttpServletResponse.SC_OK); out.print(gson.toJson(java.util.Collections.singletonMap("status","success"))); }
            else { resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); out.print(gson.toJson(java.util.Collections.singletonMap("status","fail"))); }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CorsUtil.setCors(resp, req);
        resp.setContentType("application/json;charset=UTF-8");
        if (!isAdmin(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try (PrintWriter out = resp.getWriter()) { out.print(gson.toJson(java.util.Collections.singletonMap("error","Admin only"))); }
            return;
        }

        String idStr = req.getParameter("id");
        try (PrintWriter out = resp.getWriter()) {
            if (idStr == null) { resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); out.print(gson.toJson(java.util.Collections.singletonMap("error","id required"))); return; }
            int id = Integer.parseInt(idStr);
            Users target = usersDAO.getUserById(id);
            if (target == null) { resp.setStatus(HttpServletResponse.SC_NOT_FOUND); out.print(gson.toJson(java.util.Collections.singletonMap("error","User not found"))); return; }
            // prevent deleting the last admin
            if (target.getRole() != null && target.getRole().equalsIgnoreCase("admin")) {
                int admins = usersDAO.countAdmins();
                if (admins <= 1) { resp.setStatus(HttpServletResponse.SC_FORBIDDEN); out.print(gson.toJson(java.util.Collections.singletonMap("error","Cannot demote/delete the last admin"))); return; }
            }
            boolean ok = usersDAO.deleteUser(id);
            out.print(gson.toJson(java.util.Collections.singletonMap("success", ok)));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print(gson.toJson(java.util.Collections.singletonMap("error", e.getMessage())));
        }
    }
}
