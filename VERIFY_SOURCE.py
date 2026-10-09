from pathlib import Path
import zipfile, re
src=Path('app/src/main/java/com/rmstock/nativeapp/MainActivity.java').read_text()
checks={
 'daily stock opening carries all earlier receives/uses': 'WHERE rm_code=? AND date<?' in src and 'return initial+(rec==null?0:rec.optDouble("v"))-(used==null?0:used.optDouble("v"))' in src,
 'daily report screen has requested 8 columns': all(x in src for x in ['"Item Code","Item Name","UoM","Opening","Receive","Use","Closing Balance","Status"']),
 'monthly report has requested 8 columns': all(x in src for x in ['"Opening Balance","Total Receive","Total Use"','"Closing Balance","Status"']),
 'daily PDF uses landscape table with columns': 'createLandscapeTablePdf("RM STOCK - DAILY DATE REPORT"' in src,
 'monthly PDF uses landscape table with columns': 'createLandscapeTablePdf("RM STOCK - FINAL MONTHLY USAGE"' in src,
 'both daywise reports route through same landscape generator': 'reportDaywiseLandscape(m,true)' in src and 'reportDaywiseLandscape(month,false)' in src,
 'daywise PDF canvas is A4 landscape dimensions': 'new PdfDocument.PageInfo.Builder(842,595' in src,
 'month finalization stores timestamped snapshot': 'finalized_at' in src and 'db.setSetting("finalized_month_"+month,snapshot.toString())' in src,
 'saved finalized snapshot can be viewed': 'viewFinalizedSnapshot(m)' in src and 'new JSONObject(raw)' in src,
 'Excel-compatible monthly CSV export exists': 'shareCsv(s.toString(),"Final-Monthly-Usage-"+month+".csv")' in src,
 'bottom nav has all eight requested sections': all(x in src for x in ['\\nDashboard','\\nDaily Entry','\\nBatch','\\nReports','\\nMaster','\\nRecipe','\\nCustom','\\nAI']),
 'dashboard four live cards exist': all(x in src for x in ['"Today RM Receive"','"Today Mixing-2 Use"','"Today Batch"','"Today Closing Stock"']),
}
failed=[k for k,v in checks.items() if not v]
for k,v in checks.items(): print(('PASS' if v else 'FAIL')+' '+k)
print(f'RESULT: {len(checks)-len(failed)}/{len(checks)} source checks passed')
if failed: raise SystemExit(1)
