# 隱私政策部署指南

## Google Play 上架所需 URL

Google Play Console 需要提供一個**公開可訪問的網址**指向您的隱私政策。
本專案使用 `docs/privacy-policy.html`。

## 部署選項

### 選項 1：GitHub Pages（推薦 - 免費且最簡單）

```bash
# 1. 在 GitHub 建立新 repo，例如 predictx-privacy
# 2. 把 docs/privacy/index.html 上傳到 repo
git init
git add docs/privacy/index.html
git commit -m "Add privacy policy"
git branch -M main
git remote add origin https://github.com/your-username/predictx-privacy.git
git push -u origin main

# 3. 在 GitHub repo → Settings → Pages
#    Source: Deploy from a branch
#    Branch: main, / (root)
#    Save

# 4. 取得 URL：https://your-username.github.io/predictx-privacy/
```

### 選項 2：Cloudflare Pages（推薦 - 自訂網域）

1. 把 `docs/privacy/index.html` 上傳到 GitHub repo
2. 登入 Cloudflare → Pages → Connect to Git
3. 選擇 repo，Build command 留空，Build output 設為 `docs/privacy`
4. 部署完成後得到 `https://predictx-privacy.pages.dev`

### 選項 3：Netlify Drop（最快 - 不需 Git）

1. 訪問 https://app.netlify.com/drop
2. 把 `docs/privacy/` 目錄拖到網頁
3. 取得 `https://random-name.netlify.app`

## 完成後

把 URL 填入 Google Play Console：
- 應用程式內容 → 隱私權 → 隱私權政策網址