import { GoogleGenAI } from "@google/genai";

let ai: GoogleGenAI | null = null;

if (process.env.API_KEY) {
  ai = new GoogleGenAI({ apiKey: process.env.API_KEY });
}

export const generateFinancialAdvice = async (prompt: string, contextData: string): Promise<string> => {
  if (!ai) {
    return "API Key is missing. Please configure the environment variable to use the Smart Assistant.";
  }

  try {
    const systemInstruction = `You are an expert financial assistant for the Satja Savings Ban Sai Yai Group. 
    You help officials analyze financial data, explain accounting principles (Balance Sheets, Dividend Calculation), 
    and provide insights on loan risks.
    
    Current Data Context:
    ${contextData}
    
    Keep answers concise, professional, and helpful. Format your response in Markdown.`;

    const response = await ai.models.generateContent({
      model: 'gemini-2.5-flash',
      contents: prompt,
      config: {
        systemInstruction: systemInstruction,
      }
    });

    return response.text || "I'm sorry, I couldn't generate a response at this time.";
  } catch (error) {
    console.error("Gemini API Error:", error);
    return "An error occurred while communicating with the AI service.";
  }
};
