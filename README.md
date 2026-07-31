# Ricordino 📸

**Snap a photo of anything — Ricordino turns it into a searchable note.**

Whiteboards, business cards, receipts, book pages, product labels — just take a photo, and Ricordino automatically extracts the text, categorizes it, and makes it instantly searchable. No manual typing, no cloud dependency required.

---

## ✨ Features

- **Snap & extract** — Take a photo (or pick one from your gallery) and Ricordino pulls out the text automatically using on-device OCR
- **Auto-categorization** — Notes are automatically tagged (Receipt, Contact, Recipe, Note, etc.) using lightweight rules or an optional AI call
- **Smart entity detection** — Dates, phone numbers, and addresses found in your notes can be turned into reminders
- **Full-text search** — Find any note by searching the text extracted from the photo, not just a filename
- **Private by default** — Everything is stored locally on your device; no account, no login, no data leaves your phone unless you choose to export or share

## 🧠 How it works

```
Camera / Gallery
      ↓
ML Kit Text Recognition (on-device OCR)
      ↓
Category classifier (rules-based, or optional LLM call)
      ↓
Entity detection (dates, phone numbers, addresses)
      ↓
Review & edit screen
      ↓
Room database (local storage)
```

## 🛠️ Tech stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose |
| Camera | CameraX |
| OCR | [Google ML Kit — Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition) (free, on-device) |
| Entity detection | Android `TextClassifier` (built-in, free, offline) |
| Categorization (optional) | LLM API call (text-only, low cost) — falls back to keyword rules if no API key is set |
| Local storage | Room (SQLite) |
| Photo storage | App-private internal storage (`context.filesDir`) — no storage permissions required |

## 📋 Requirements

- Android Studio (latest stable)
- Android SDK 24+ (Android 7.0)
- No API keys required for core OCR functionality — everything works fully offline out of the box

## 🚀 Getting started

```bash
git clone https://github.com/<your-username>/ricordino.git
cd ricordino
```

Open the project in Android Studio and run it on an emulator or physical device. No configuration needed for the core OCR + notes flow.

### Optional: enable AI-powered categorization

If you want smarter categorization beyond the built-in keyword rules, add your API key to `local.properties`:

```
LLM_API_KEY=your_key_here
```

This is entirely optional — the app is fully functional without it.

## 🗺️ Roadmap

- [x] Camera capture + on-device OCR
- [x] Local notes database + search
- [x] Keyword-based auto-categorization
- [ ] LLM-based smart categorization
- [ ] Reminders from detected dates
- [ ] Export notes to PDF/CSV
- [ ] Multi-language OCR support

## 📄 License

MIT — see [LICENSE](LICENSE) for details.

## 🤝 Contributing

Issues and pull requests are welcome! If you have ideas for new categories, better entity detection, or UI improvements, feel free to open an issue.
