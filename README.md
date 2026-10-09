# Furqan AI Backend

This is the real-AI server for the Android app.

1. Install Node.js 20+.
2. Run `npm install`.
3. Set `OPENAI_API_KEY` as a server environment variable.
4. Optionally set `OPENAI_MODEL`.
5. Run `npm start`.
6. Deploy this backend on a service that gives you an HTTPS URL.
7. Put that HTTPS `/chat` URL into MainActivity.kt as BACKEND_URL.
8. Rebuild the Android app.

SECURITY:
Never put OPENAI_API_KEY in the Android APK. OpenAI's API documentation explicitly recommends keeping API keys secret and loading them server-side.
