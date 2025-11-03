# Status Logic Implementation - Consistent Rules Across Backend & Frontend

**Date**: December 2024  
**Objective**: Implement consistent 4-rule precedence system to ensure status badges always match recommendation text

---

## Overview

Previously, the status badge (frontend) and recommendation text (backend) could contradict each other. For example:
- ❌ **Before**: Station with 3 swaps showing "Critical" badge with "insufficient data" recommendation
- ✅ **After**: Station with 3 swaps always shows "⚠️ DATA" badge with matching "insufficient data" recommendation

---

## Strict Precedence Rules (Priority Order)

### Rule 1: Insufficient Data → WARNING_DATA (Gray Badge)
**Condition**: `sampleSize < 5` transactions in 14 days

**Backend** (`StationUpgradeSuggestionController.java` lines 220-231):
```java
if (sampleSize < 5) {
    item.put("status", "WARNING_DATA");
    item.put("recommendation", "⚠️ Không đủ dữ liệu để dự báo chính xác");
    item.put("evidence", "Chỉ có " + sampleSize + " giao dịch trong 14 ngày...");
    item.put("dataInsufficient", true);
    results.add(item);
    continue; // Skip to next station
}
```

**Frontend** (`web/admin/index.html` lines 247-251):
```javascript
// RULE 1: Insufficient data (WARNING_DATA)
if (sampleSize < 5) {
    return { cls: 'warning-data', label: '⚠️ DATA' };
}
```

**Visual**: 🟫 Gray badge with "⚠️ DATA"

---

### Rule 2: Critical Conditions → CRITICAL (Red Badge)
**Conditions** (ANY of these):
- `failRate >= 20%` (1 in 5 swaps fail)
- `overloadDays >= 2` (2+ days overloaded in 14 days)
- `last7Avg >= slotCapacity` (average demand exceeds capacity)

**Backend** (`StationUpgradeSuggestionController.java` lines 247-264):
```java
boolean isCritical = false;

if (failRate >= 0.20) {
    recs.add("🚨 KHẨN CẤP: Tỷ lệ lỗi rất cao...");
    isCritical = true;
}
if (overload >= 2) {
    recs.add("🚨 KHẨN CẤP: Quá tải " + overload + " ngày...");
    isCritical = true;
}
if (last7Avg >= slotCap) {
    recs.add("🚨 KHẨN CẤP: Nhu cầu vượt capacity...");
    isCritical = true;
}

if (isCritical) {
    item.put("status", "CRITICAL");
}
```

**Frontend** (`web/admin/index.html` lines 257-260):
```javascript
// RULE 2: CRITICAL
if (failRate >= 20 || overload >= 2 || last7Avg >= slotCap) {
    return { cls: 'critical', label: '🚨 CRITICAL' };
}
```

**Visual**: 🔴 Red badge with "🚨 CRITICAL"

---

### Rule 3: Warning Conditions → WARNING (Orange Badge)
**Conditions** (ANY of these):
- `failRate >= 10%` (10-19% fail rate)
- `overloadDays == 1` (exactly 1 day overloaded)
- `last7Avg >= slotCapacity * 0.8` (80-99% capacity)
- `growthPercent >= 50%` (rapid growth week-over-week)

**Backend** (`StationUpgradeSuggestionController.java` lines 269-290):
```java
boolean isWarning = false;

if (!isCritical && failRate >= 0.10) {
    recs.add("⚠️ CẢNH BÁO: Tỷ lệ lỗi cao...");
    isWarning = true;
}
if (!isCritical && overload == 1) {
    recs.add("⚠️ CẢNH BÁO: Có 1 ngày quá tải...");
    isWarning = true;
}
if (!isCritical && last7Avg >= slotCap * 0.8) {
    recs.add("⚠️ CẢNH BÁO: Công suất cao...");
    isWarning = true;
}
if (!isCritical && growth >= 50.0) {
    recs.add("⚠️ CẢNH BÁO: Tăng trưởng nhanh...");
    isWarning = true;
}

if (isWarning) {
    item.put("status", "WARNING");
}
```

**Frontend** (`web/admin/index.html` lines 263-266):
```javascript
// RULE 3: WARNING
if (failRate >= 10 || overload === 1 || last7Avg >= slotCap * 0.8 || growth >= 50) {
    return { cls: 'warning', label: '⚠️ WARNING' };
}
```

**Visual**: 🟠 Orange badge with "⚠️ WARNING"

---

### Rule 4: Healthy Station → OK (Green Badge)
**Conditions**: All metrics within safe range (no conditions from Rules 1-3)

**Backend** (`StationUpgradeSuggestionController.java` lines 295-299):
```java
if (!isCritical && !isWarning) {
    item.put("status", "OK");
    recs.add("✅ Trạm hoạt động ổn định");
    evidence.append("Tất cả chỉ số trong ngưỡng an toàn. ");
}
```

**Frontend** (`web/admin/index.html` lines 269-270):
```javascript
// RULE 4: OK - all metrics healthy
return { cls: 'ok', label: '✅ OK' };
```

**Visual**: 🟢 Green badge with "✅ OK"

---

## Backend Changes

### File: `StationUpgradeSuggestionController.java`

**Lines 220-231**: Rule 1 - Insufficient data validation
- Added explicit `status: "WARNING_DATA"` field to JSON response
- Threshold: `sampleSize < 5` (previously was 3)
- Uses `continue` to skip status evaluation when data insufficient

**Lines 247-299**: Rules 2-4 - Priority-based status determination
- Replaced old heuristic logic with strict if-else precedence
- Each rule explicitly sets `status` field in JSON
- Critical and Warning conditions evaluated independently with flags
- Prevents contradictions (e.g., "Critical" badge with "no upgrade needed" text)

**Lines 315-318**: Simplified recommendation assignment
- Always joins recommendation list (no empty check)
- Status already set by Rules 1-4

---

## Frontend Changes

### File: `web/admin/index.html`

**Lines 233-275**: `computeBadge()` function - Complete rewrite
- First checks explicit `item.status` field from backend (if available)
- Falls back to local computation using identical logic as backend
- Matches all 4 rules with exact same thresholds
- Added support for new `WARNING_DATA` status

**Lines 313-317**: Badge color mapping
- Added `warning-data` class → Gray (#9e9e9e)
- Updated to 4-level system (was 3-level)

**Lines 292-295**: Legend update
- Added `⚠️ DATA` badge to legend
- Explains "Không đủ dữ liệu" (insufficient data)
- Shows all 4 status levels in order

**Lines 324**: Insufficient data threshold
- Updated from `sampleSize < 3` to `sampleSize < 5`
- Matches backend Rule 1

**Lines 367-371**: CSV export sorting
- Added `warning-data` priority (lowest)
- Sort order: Critical (4) > Warning (3) > OK (2) > WARNING_DATA (1)

---

## Key Improvements

### 1. Explicit Status Field
- Backend now returns `status` field in JSON (`WARNING_DATA`, `CRITICAL`, `WARNING`, `OK`)
- Frontend checks this field first before computing locally
- Ensures 100% consistency between backend and frontend

### 2. Strict Precedence
- Rules evaluated in order 1 → 2 → 3 → 4
- Rule 1 uses `continue` to skip further evaluation
- Rules 2-3 use `!isCritical` guards to prevent overriding
- No ambiguity or contradictions

### 3. Threshold Alignment
- **Insufficient data**: 3 → 5 transactions (stricter)
- **Critical fail rate**: 20% (was 10%)
- **Warning fail rate**: 10% (new threshold)
- **Critical overload**: 2+ days (was 3)
- **Warning overload**: exactly 1 day (new)
- **Critical capacity**: 100% (was 90%)
- **Warning capacity**: 80% (was 60%)

### 4. Visual Clarity
- New gray badge for insufficient data (not confused with yellow warning)
- Emoji in badge labels (⚠️ DATA, ✅ OK, ⚠️ WARNING, 🚨 CRITICAL)
- Legend explains all 4 levels

---

## Testing Scenarios

| Scenario | sampleSize | failRate | overload | last7Avg | capacity | Expected Status | Badge | ✅ |
|----------|-----------|----------|----------|----------|----------|----------------|-------|-----|
| New station | 3 | 0% | 0 | 1.2 | 5 | WARNING_DATA | ⚠️ DATA | ✅ |
| High fail rate | 10 | 25% | 0 | 2.5 | 5 | CRITICAL | 🚨 CRITICAL | ✅ |
| Overloaded 2 days | 20 | 5% | 2 | 4.8 | 5 | CRITICAL | 🚨 CRITICAL | ✅ |
| Demand > capacity | 30 | 3% | 0 | 5.5 | 5 | CRITICAL | 🚨 CRITICAL | ✅ |
| 1 day overload | 15 | 2% | 1 | 3.0 | 5 | WARNING | ⚠️ WARNING | ✅ |
| 80% capacity | 25 | 0% | 0 | 4.0 | 5 | WARNING | ⚠️ WARNING | ✅ |
| Rapid growth | 18 | 0% | 0 | 2.0 | 5 | WARNING (growth=60%) | ⚠️ WARNING | ✅ |
| Healthy station | 50 | 2% | 0 | 2.5 | 5 | OK | ✅ OK | ✅ |

---

## Build & Deploy

**Build**:
```bash
cd c:\AK\HOCKI5\SWP391\Code\TestAIChatBox\SWP391_Backend\Backend\webAPI
ant clean dist
```

**Deploy**:
```powershell
Copy-Item "dist\webAPI.war" "C:\AK\PRJ301\apache-tomcat-10.0.27-windows-x64\apache-tomcat-10.0.27\webapps\" -Force
```

**Result**: ✅ BUILD SUCCESSFUL (5 seconds) - Deployed to Tomcat

---

## Verification Checklist

- [✅] Backend Rule 1: sampleSize < 5 → WARNING_DATA
- [✅] Backend Rule 2: failRate >= 20% OR overload >= 2 OR last7Avg >= capacity → CRITICAL
- [✅] Backend Rule 3: failRate >= 10% OR overload == 1 OR last7Avg >= 80% OR growth >= 50% → WARNING
- [✅] Backend Rule 4: Else → OK
- [✅] Frontend `computeBadge()` matches backend rules exactly
- [✅] Badge color mapping includes `warning-data` (gray)
- [✅] Legend shows all 4 status levels
- [✅] Insufficient data threshold updated to < 5 (frontend)
- [✅] CSV export sorts by 4-level priority
- [✅] Build successful without errors
- [✅] Deployed to Tomcat

---

## Status Badge Reference

| Status | Class | Color | Label | Meaning |
|--------|-------|-------|-------|---------|
| Insufficient data | `warning-data` | Gray (#9e9e9e) | ⚠️ DATA | < 5 transactions, can't analyze |
| Healthy | `ok` | Green (#4caf50) | ✅ OK | All metrics safe |
| Needs monitoring | `warning` | Orange (#ff9800) | ⚠️ WARNING | Approaching limits |
| Urgent action | `critical` | Red (#f44336) | 🚨 CRITICAL | Exceeds safe thresholds |

---

## Result

**Status badges and recommendation text now 100% consistent across all scenarios.**

No more contradictions like:
- ❌ "Critical" badge + "insufficient data to forecast" text
- ❌ "OK" badge + "urgent upgrade needed" text

All status determinations follow strict precedence: **Insufficient data → Critical → Warning → OK**
