module.exports = async function () {
  const token = process.env.WEBMENTION_IO_TOKEN;
  if (!token) return {};
  const url = `https://webmention.io/api/mentions.jf2?domain=chndr.me&token=${token}&per-page=1000`;
  let data;
  try {
    const res = await fetch(url);
    data = await res.json();
  } catch (e) {
    console.error("Failed to fetch webmentions:", e.message);
    return {};
  }
  const result = {};
  for (const mention of data.children || []) {
    const target = mention["wm-target"];
    if (!result[target]) result[target] = { likes: [], reposts: [], replies: [] };
    const prop = mention["wm-property"];
    if (prop === "like-of") result[target].likes.push(mention);
    else if (prop === "repost-of") result[target].reposts.push(mention);
    else if (prop === "in-reply-to") result[target].replies.push(mention);
  }
  return result;
};
