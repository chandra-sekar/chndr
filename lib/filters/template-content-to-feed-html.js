const {
  absoluteUrl,
  convertHtmlToAbsoluteUrls,
} = require("@11ty/eleventy-plugin-rss");

/**
 * Get content of a post as HTML (suitable for RSS feed readers)
 *
 * @param {string} item Post data
 * @returns {string} Decorate HTML text
 */
module.exports = async (item, metadata) => {
  //const { app } = item.data;
  let content = "";

  // Remove line breaks from template content
  content += item.templateContent.replace(/\n/g, "");
  // If photo post, present each photo within a figure
  if (item.data.photo) {
    for (const photo of item.data.photo) {
      content += `<figure><img src="${absoluteUrl(
        photo.url,
        metadata.url
      )}" alt="${photo.alt}"></figure>`;
    }
  }

  // If bookmark post, append the bookmarked URL as a link
  if (item.data["bookmark-of"]) {
    content += `<p><a href="${item.data["bookmark-of"]}">→ ${item.data["bookmark-of"]}</a></p>`;
  }

  // Convert relative URLs to absolute URLs
  const absolutePostUrl = new URL(item.url, metadata.url).href;
  content = await convertHtmlToAbsoluteUrls(content, absolutePostUrl);
  return content;
};
