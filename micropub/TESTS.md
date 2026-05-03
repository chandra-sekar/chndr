# Micropub Server Tests

Behavioral tests that verify HTTP responses and post content — not implementation
internals. Tests mock external dependencies (IndieAuth, GitHub API) at the function
boundary so the implementation can be freely refactored.

## Test runner

```
clojure -X:test
```

## Dependencies

- `ring/ring-mock` — constructs Ring requests without a running server
- `org.clojure/test.check` — not needed for now

## Test files

### `test/micropub/handler_test.clj`

Tests the full HTTP request/response cycle via the Ring handler.
External calls (`auth/validate-token`, `posts/create-post`) are stubbed with `with-redefs`.

#### Health check

- [ ] `HEAD /` returns 200
- [ ] `GET /` returns 200
- [ ] `GET /unknown-path` returns 404

#### Micropub config

- [ ] `GET /micropub?q=config` returns 200
- [ ] `GET /micropub?q=config` returns `Content-Type: application/json`
- [ ] `GET /micropub?q=config` returns body `{}`
- [ ] `GET /micropub?q=config&extra=foo` also returns config (extra query params don't break it)

#### Authentication

- [ ] `POST /micropub` with no `Authorization` header returns 401
- [ ] `POST /micropub` with an invalid token returns 403

#### Form-encoded post creation

- [ ] Valid token + `content=...` returns 201
- [ ] Note (no `name`) returns `Location: https://chndr.cc/notes/<timestamp>/`
- [ ] Article (`name` + `content`) returns `Location: https://chndr.cc/posts/<slug>/`
- [ ] Missing `content` returns 400

#### JSON post creation

- [ ] JSON note (`type: h-entry`, no `name` property) returns 201
- [ ] JSON article (`name` + `content` under `properties`) returns 201
- [ ] JSON with `content` as `{"html": "..."}` map returns 201 (not 400)

#### GitHub failure

- [ ] When `posts/create-post` returns `:error`, server returns 500

---

### `test/micropub/posts_test.clj`

Tests the content that would be committed to GitHub — file paths, frontmatter, and
body — by calling `posts/build-note` and `posts/build-article` directly (or via a
refactored pure helper). Does **not** make real GitHub API calls.

#### Note structure

- [ ] File path matches `notes/<unix-timestamp>.md`
- [ ] Frontmatter contains `date:` and nothing else
- [ ] Body follows the frontmatter

#### Article structure

- [ ] File path matches `posts/<slug>.md`
- [ ] Frontmatter contains `title:`, `date:`, and `layout:`
- [ ] Slug lowercases the title
- [ ] Slug replaces spaces with hyphens
- [ ] Slug strips special characters
- [ ] Body follows the frontmatter
