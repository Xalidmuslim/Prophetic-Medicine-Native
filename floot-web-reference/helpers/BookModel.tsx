export namespace BookModel {
  export type BlockType = "text" | "quran" | "hadith" | "historical_note" | "subheading";

  export interface ContentBlock {
    id: string;
    type: BlockType;
    text: string;
    anchor: string;
    level?: number;
  }

  export interface Chapter {
    id: string;
    slug: string;
    title: string;
    order: number;
    section: string;
    contentBlocks: ContentBlock[];
    topics: string[];
    remedies: string[];
    previousChapterId: string | null;
    nextChapterId: string | null;
  }

  export interface Topic {
    id: string;
    title: string;
    chapterIds: string[];
  }

  export interface Remedy {
    id: string;
    title: string;
    aliases: string[];
    chapterIds: string[];
    anchors: string[];
  }

  export interface Book {
    title: string;
    author: string;
    chapters: Chapter[];
    topics: Topic[];
    remedies: Remedy[];
    stats: {
      sourceCharacters: number;
      sourceWords: number;
      chapters: number;
      blocks: number;
      topics: number;
      remedies: number;
    };
  }

  export interface SearchResult {
    chapterId: string;
    chapterSlug: string;
    chapterTitle: string;
    anchor: string;
    type: BlockType;
    snippet: string;
  }
}