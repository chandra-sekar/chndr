# chndr.cc

Chandrasekar's personal website built with [Eleventy](https://www.11ty.dev/).

## Features

- **Journal**: Long-form blog posts with titles
- **Notes**: Short-form content (tweet-like) with timestamps, image support, and links
- **Bookshelf**: Curated collection of favorite content
- **IndieWeb**: Microformats support (h-entry, h-card)
- **RSS/JSON Feeds**: Available for both posts and notes
- **Sass**: Compiled SCSS stylesheets
- **Netlify CMS**: Admin interface for content management

## Prerequisites

- Node.js (v18 or higher recommended - see `.nvmrc`)
- npm

## Getting Started

### Installation

1. Clone the repository:
```bash
git clone https://github.com/chandra-sekar/chndr.git
cd chndr
```

2. Install dependencies:
```bash
npm install
```

### Development

Start the development server with live reload:

```bash
npm run dev
```

The site will be available at `http://localhost:8080`

### Building

Build the site for production:

```bash
npm run build
```

The output will be in the `_site` directory.

### Other Commands

- `npm start` - Alias for `npm run dev`
- `npm run clean` - Remove the `_site` directory

## Project Structure

```
.
├── _11ty/              # Eleventy utilities
│   └── getTagList.js   # Tag collection helper
├── _data/              # Global data files
│   └── metadata.json   # Site metadata
├── _includes/          # Templates and partials
│   ├── layouts/        # Layout templates
│   │   ├── base.njk    # Base HTML layout
│   │   ├── home.njk    # Homepage layout
│   │   ├── note.njk    # Note layout
│   │   ├── page.njk    # Page layout
│   │   └── post.njk    # Blog post layout
│   ├── noteslist.njk   # Notes listing component
│   └── postslist.njk   # Posts listing component
├── admin/              # Netlify CMS configuration
│   └── config.yml      # CMS config
├── css/                # Stylesheets (SCSS)
│   ├── _base.scss
│   ├── _reset.scss
│   ├── _theme.scss
│   ├── _utilities.scss
│   ├── _variables.scss
│   └── style.scss      # Main stylesheet
├── feed/               # RSS/JSON feed templates
│   ├── feed.njk        # XML RSS feed
│   └── feed-notes.njk  # JSON feed for notes
├── img/                # Images
├── lib/                # Custom functions
│   ├── filters/        # Custom Eleventy filters
│   └── shortcodes/     # Custom Eleventy shortcodes
├── notes/              # Note entries (short-form)
│   ├── notes.json      # Directory data file
│   └── *.md            # Individual notes
├── posts/              # Blog posts (long-form)
│   ├── posts.json      # Directory data file
│   └── *.md            # Individual posts
├── about/              # About page
├── bookshelf/          # Bookshelf page
├── journal/            # Journal listing page
├── .eleventy.js        # Eleventy configuration
└── package.json        # Dependencies and scripts
```

## Content Management

### Creating a Blog Post

Create a new markdown file in the `posts/` directory:

```markdown
---
title: "Your Post Title"
date: 2025-11-08
layout: layouts/post.njk
tags:
  - posts
  - your-tag
---

Your post content here...
```

### Creating a Note

Create a new markdown file in the `notes/` directory:

```markdown
---
date: 2025-11-08T09:00:00.000+00:00
photo:
  - url: /img/your-image.jpg
    alt: "Image description"
---

Your short note content. Can include [links](https://example.com) and **formatting**.
```

### Using Netlify CMS

Access the admin interface at `/admin/` when running locally or on your deployed site. You'll need to authenticate with Netlify Identity.

## Configuration

### Site Metadata

Edit `_data/metadata.json` to update site information:

```json
{
  "title": "Your Site Title",
  "url": "https://yoursite.com",
  "description": "Site description"
}
```

### Eleventy Config

The main configuration is in `.eleventy.js`. Key features:

- **Filters**: Custom date formatting, array helpers
- **Collections**: Auto-generated from tags
- **Passthrough Copy**: Images, fonts, static assets
- **Markdown**: markdown-it with anchor links
- **SCSS**: Automatic compilation to CSS

## Deployment

The site is configured for deployment on Netlify:

1. Push changes to your GitHub repository
2. Netlify will automatically build and deploy
3. Build command: `npm run build`
4. Publish directory: `_site`

## Feeds

- **RSS Feed (Posts)**: `/feed/feed.xml`
- **JSON Feed (Notes)**: `/feed/feed-notes.json`

## IndieWeb Features

This site supports IndieWeb standards:

- **Microformats**: h-entry, h-card, dt-published
- **Micropub**: Endpoint configured in base template
- **IndieAuth**: Authentication endpoints configured
- **rel=me**: Links for identity verification

## Dependencies

### Core
- `@11ty/eleventy` - Static site generator
- `luxon` - Date/time handling
- `sass` - CSS preprocessing

### Plugins
- `@11ty/eleventy-plugin-rss` - RSS feed generation
- `@11ty/eleventy-plugin-syntaxhighlight` - Code syntax highlighting

### Markdown
- `markdown-it` - Markdown parser
- `markdown-it-anchor` - Auto-generate heading anchors

## Browser Support

Modern browsers (last 2 versions):
- Chrome
- Firefox
- Safari
- Edge

## License

ISC

## Author

Chandrasekar - [https://chndr.cc](https://chndr.cc)

## Links

- [Repository](https://github.com/chandra-sekar/chndr)
- [Issues](https://github.com/chandra-sekar/chndr/issues)
- [Eleventy Documentation](https://www.11ty.dev/docs/)
