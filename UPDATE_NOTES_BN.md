# RM-Stock — সোর্স আপডেট ও যাচাই নোট

এই প্যাকেজটি বিদ্যমান Native Android প্রজেক্টের সোর্স। ডাটাবেসের বাস্তব ডেটা এই ZIP-এ নেই। ইনস্টল করার আগে বর্তমান অ্যাপের JSON backup রাখুন।

## অন্তর্ভুক্ত আচরণ
- Opening = Master opening + নির্বাচিত দিনের আগের সব Receive − আগের সব Mixing-2 Use; তাই মাস/বছর বদলালে হিসাব শূন্যে রিসেট হয় না।
- Daily Date Report-এর স্ক্রিনে Item Code, Item Name, UoM, Opening, Receive, Use, Closing Balance, Status; নির্বাচিত দিনের PDF আলাদা landscape table-এ একই আটটি কলাম ব্যবহার করে।
- Final Monthly Usage-এ আটটি কলাম, CSV export এবং PDF/print আছে। Monthly PDF-ও landscape table format-এ একই আটটি কলাম দেয়।
- Finalize Month (Admin) timestamp-সহ fixed JSON snapshot settings-এ সংরক্ষণ করে; এখন স্ক্রিন থেকে snapshot-এর saved status এবং সংরক্ষিত snapshot দেখা যায়। এটি live data-entry বন্ধ করে না; পরে live report বদলালেও snapshot অপরিবর্তিত থাকে, যতক্ষণ না আবার finalize করা হয়।
- `RM Receive Monthly — Day-wise` ও `RM Mixing-2 Monthly — Day-wise`-এর screen/table আচরণ অপরিবর্তিত; তাদের PDF generator A4 landscape (842×595 pt), 1–31 date columns, repeated header/title এবং page numbering ব্যবহার করে।
- Dashboard-এর চার live cards, ticker, Stock Overview এবং আটটি bottom-nav section রাখা হয়েছে।

## এই পরিবেশে করা যাচাই
- `VERIFY_SOURCE.py`: 12/12 source-level checks পাস করেছে।
- ZIP integrity check করা হয়েছে।
- Android APK build **পাস করেনি/সম্পন্ন হয়নি**: Gradle 8.2 distribution ডাউনলোডের জন্য `services.gradle.org`-এ network/DNS সংযোগ পাওয়া যায়নি। এই কারণে এই প্যাকেজকে build-verified বা device-tested বলা হচ্ছে না। APK তৈরি ও বাস্তব ডিভাইসে যাচাইয়ের জন্য Android SDK এবং Gradle distribution-সহ build environment প্রয়োজন।

## সতর্কতা
এই source-level checks Android compiler/build বা on-device test-এর বিকল্প নয়। Production-এ ব্যবহারের আগে APK build, install, stock carry-forward, PDF/CSV output, month-finalization এবং navigation-কে বাস্তব test data দিয়ে যাচাই করুন।
