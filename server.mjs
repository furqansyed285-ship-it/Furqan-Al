import express from "express";
import OpenAI from "openai";

const app = express();
app.use(express.json({ limit: "64kb" }));

const port = process.env.PORT || 3000;
const model = process.env.OPENAI_MODEL || "gpt-5.6";

if (!process.env.OPENAI_API_KEY) {
  console.error("Missing OPENAI_API_KEY environment variable.");
  process.exit(1);
}

const client = new OpenAI({ apiKey: process.env.OPENAI_API_KEY });

app.get("/health", (req,res) => res.json({ok:true, service:"Furqan AI"}));

app.post("/chat", async (req,res) => {
  try {
    const message = String(req.body?.message || "").trim();
    if (!message) return res.status(400).json({error:"message is required"});
    if (message.length > 12000) return res.status(413).json({error:"message too long"});

    const response = await client.responses.create({
      model,
      input: [
        {
          role: "system",
          content: "You are Furqan AI, a helpful multilingual assistant. Reply clearly and naturally. If the user writes Urdu or Roman Urdu, reply in the same style unless asked otherwise."
        },
        { role: "user", content: message }
      ]
    });

    res.json({reply: response.output_text || "I could not generate a response."});
  } catch (err) {
    console.error(err);
    res.status(500).json({error:"AI request failed"});
  }
});

app.listen(port, () => console.log(`Furqan AI backend listening on ${port}`));
