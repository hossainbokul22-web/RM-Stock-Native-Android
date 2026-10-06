# RM Stock — Full Native Android

This project is a native Android rebuild. It does not use WebView, PWA, localStorage, or a web UI.

## Native architecture
- Java Android UI (programmatic native Views)
- SQLiteOpenHelper offline data layer
- Dynamic RM/Product master
- Dynamic RM × Product recipe matrix
- Native batch and receive entry with editable Batch Count per saved Shift/Product entry
- Native stock calculations
- Native offline camera + Tesseract OCR (English + Bengali)
- Single Owner/Admin AI draft approval into Daily Entry
- Native PdfDocument report generation
- Android PrintManager printing
- Android system document picker for saving PDF
- App-private storage for database, OCR files, camera cache and generated reports
- Native Custom Sheet definitions/rows stored in SQLite

## Dynamic guarantees
- New RM automatically gets recipe rows with quantity 0.
- New Product automatically gets recipe rows with quantity 0 for every RM.
- RM/Product code edits migrate dependent recipe/batch references.
- Business data is stored in SQLite, not hardcoded into screens.

## Build
The repository contains a GitHub Actions workflow that builds a debug APK with Gradle 8.2 and Java 17.


## Final behavior notes
- Personal-use mode has one Owner/Admin only; there is no Member account/role workflow.
- Saved Batch Count entries can be edited. If Shift 1 was entered as 4 but the actual count was 5, Edit changes 4 → 5 in the same `batches` record.
- The edited Batch Count continues through the existing recipe-linked consumption calculation; no separate calculation path is introduced.
- RM/Product names and codes remain dynamic. Product code edits migrate recipe and historical batch references.
- Custom Sheet names remain editable by internal Sheet ID, so renaming does not create a new sheet.

### Batch Count rule
- Batch Count means the total number of batches produced in that Shift for the selected Product and date.
- It is a whole-number value and is editable after saving.
- There is exactly one saved count for each Date + Product + Shift. Saving again replaces that count; it never adds another batch-count row.
- Example: Shift 1 entered as 4, then Edit → 5. Stock usage becomes `5 × recipe quantity` through the same existing calculation path.
- Changing 5 back to 4 similarly recalculates from 4.

## Custom Sheet designer
- Create sheets with a stable internal Sheet ID.
- Rename sheet name/description without changing the Sheet ID or existing rows.
- Add, rename, delete, and reorder columns.
- Column types: text, number, date, formula.
- Formula columns are read-only in row entry and are recalculated from the same row data.
- Supported formula patterns include arithmetic (`=B2*C2`), `=SUM(D2:F2)`, and basic `=IF(E2<0,"SHORTAGE","OK")` conditions.
- Renaming a column migrates existing row JSON keys so saved data is retained.
- Sheets and their rows can be deleted from the Owner-only native designer.

## Completion audit additions
- Daily Receive entries can be edited after saving.
- RM code changes migrate both recipe and historical receive references.
- Reports use the first-day opening calculation for monthly opening.
- Mixing-2 view shows batch × recipe RM usage and links to the same editable batch count.
- Custom Sheet rows can be deleted; columns can be moved up/down; formula evaluation supports multi-letter columns and repeated dependency passes.
- Company name can be edited from Reports.
- JSON backup/restore covers operational and custom tables; CSV export is available.
- PDF output has page numbers, simple line wrapping, company header, and native Android Print/Save/Share flow.

## GitHub upload/build
Upload the **contents of this project folder** to the repository root (not the ZIP file itself). The repository must contain `settings.gradle`, `build.gradle`, `app/`, and `.github/workflows/build-apk.yml` at the root. GitHub Actions installs Android SDK 34 and Gradle 8.2, then builds `app-debug.apk`.


### Historical Mixing-2 Batch Log
The project keeps a native SQLite `mixing2_batch_log` snapshot for each saved Date + Product + Shift + RM combination. Correcting a batch count updates that batch log; later recipe edits do not rewrite the historical recipe quantity/consumption already recorded in the log. The normal stock-usage calculation still uses the current Batch Count × Recipe logic.
