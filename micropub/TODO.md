# Micropub Server Todos

## Setup

- [x] Create `micropub/garden.edn`
- [x] Create `micropub/deps.edn` with http-kit, data.json, and `:nextjournal/garden` alias
- [x] Create `micropub/src/micropub/core.clj` (entry point, starts http-kit server on port 7777)

## Server

- [x] `HEAD /` and `GET /` return 200 (health check)
- [x] `GET /micropub?q=config` returns `{}`
- [x] `POST /micropub` — parse form-encoded body
- [x] `POST /micropub` — parse JSON body
- [x] Return 401 if no Bearer token present
- [x] Return 403 if token is invalid or `me` does not match `https://chndr.cc/`
- [x] Return 400 for malformed requests

## Auth

- [x] Token validation via `POST https://tokens.indieauth.com/token`
- [x] Verify `me` field in response matches `https://chndr.cc/`

## Post creation

- [x] Distinguish note vs article (presence of `name` property)
- [x] Generate note filename: `notes/<unix-timestamp>.md`
- [x] Generate article filename: `posts/<slug>.md` (slugify `name`)
- [x] Build note frontmatter (date only)
- [x] Build article frontmatter (title, date, layout)
- [x] Base64-encode file content for GitHub API
- [x] `PUT` to GitHub Contents API
- [x] Return `201 Created` with correct `Location` header

## Site integration

- [x] Add `<link rel="micropub">` to `_includes/layouts/base.njk`
- [x] Add `<link rel="authorization_endpoint">` to `_includes/layouts/base.njk` (was already present)
- [x] Add `<link rel="token_endpoint">` to `_includes/layouts/base.njk` (was already present)

## Deployment

- [x] Register SSH public key with application.garden (web UI → account settings)
- [x] Create GitHub PAT with `repo` scope for `chandra-sekar/chndr` and add via `garden secrets add GITHUB_TOKEN`
- [x] Deploy with `garden deploy` from the `micropub/` directory
- [x] Verify health check passes (`HEAD https://chndr-micropub.apps.garden/`)
- [ ] Test with a micropub client (e.g. Quill at quill.p3k.io)
