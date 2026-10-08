import { parseBook } from "./parseBook";

describe("parseBook", () => {
  it("preserves paragraphs while creating stable chapter navigation", () => {
    const source = "# Первая глава\n\nПервый абзац.\n\n## Подзаголовок\n\nВторой абзац.\n\n# Вторая глава\n\nТретий абзац.";
    const book = parseBook(source);
    expect(book.chapters).toHaveSize(2);
    expect(book.chapters[0]?.nextChapterId).toBe(book.chapters[1]?.id);
    expect(book.chapters[1]?.previousChapterId).toBe(book.chapters[0]?.id);
    const joined = book.chapters.flatMap((c) => c.contentBlocks.map((b) => b.text)).join(" ");
    expect(joined).toContain("Первый абзац.");
    expect(joined).toContain("Второй абзац.");
    expect(joined).toContain("Третий абзац.");
  });
});