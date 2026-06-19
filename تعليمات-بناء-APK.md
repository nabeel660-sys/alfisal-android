# كيف تبني APK بدون Android Studio - خطوة بخطوة

## الطريقة الأسهل: GitHub Actions (مجاني 100%)

### الخطوة 1: أنشئ حساب GitHub
1. اذهب إلى https://github.com
2. اضغط Sign Up وأنشئ حساباً مجانياً

### الخطوة 2: ارفع المشروع
1. اضغط على زر **+** ثم **New repository**
2. اسم المشروع: `alfisal-android`
3. اضغط **Create repository**
4. ارفع جميع ملفات هذا المجلد إلى GitHub

### الخطوة 3: احصل على APK
1. اذهب إلى تبويب **Actions** في المشروع
2. ستجد Workflow اسمه **Build APK & AAB** يعمل تلقائياً
3. انتظر 5-10 دقائق حتى ينتهي
4. اضغط على الـ workflow ثم **Artifacts**
5. حمّل **alfisal-debug.apk** ← هذا الـ APK جاهز للتثبيت!

---

## للحصول على Release APK موقّع (للنشر على Google Play)

### أولاً: أنشئ Keystore (مرة واحدة فقط)
افتح Terminal وشغّل هذا الأمر:
```bash
keytool -genkey -v -keystore alfisal-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias alfisal
```
احتفظ بهذا الملف في مكان آمن جداً!

### ثانياً: ضع Keystore في GitHub Secrets
1. افتح GitHub → Settings → Secrets → Actions
2. أضف هذه الـ Secrets:
   - `KEYSTORE_BASE64`: شغّل `base64 -i alfisal-keystore.jks | pbcopy` وألصق النتيجة
   - `KEYSTORE_PASSWORD`: كلمة المرور التي اخترتها
   - `KEY_ALIAS`: `alfisal`
   - `KEY_PASSWORD`: كلمة مرور المفتاح

### ثالثاً: شغّل الـ Workflow
اذهب إلى Actions → Build APK & AAB → Run workflow

---

## تثبيت APK على الهاتف
1. حمّل ملف APK على هاتفك
2. اذهب إلى الإعدادات → الأمان → السماح بمصادر غير معروفة
3. افتح ملف APK واضغط تثبيت

---

## مميزات التطبيق
✅ Splash Screen أزرق مع الشعار (ثانيتان)
✅ WebView يعرض الموقع بدون شريط متصفح
✅ دعم رفع الصور من الكاميرا والمعرض
✅ زر الرجوع يعمل بشكل صحيح
✅ رسالة عند انقطاع الإنترنت
✅ وضع رأسي فقط
✅ أيقونة احترافية زرقاء
