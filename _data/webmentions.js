module.exports = async function () {
  const token = process.env.WEBMENTION_IO_TOKEN;
  if (!token) return {};
  const url = `https://webmention.io/api/mentions.jf2?domain=chndr.cc&token=${token}&per-page=1000`;
  const res = await fetch(url);
  const data = await res.json();
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
