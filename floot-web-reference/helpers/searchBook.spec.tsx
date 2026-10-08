import { searchBook } from "./searchBook";
import { BookModel } from "./BookModel";

const book: BookModel.Book = {
  title: "Тест",
  author: "Автор",
  chapters: [{
    id:"ch-001", slug:"chernyy-tmin", title:"Чёрный тмин", order:1, section:"",
    topics:[], remedies:["black-seed"], previousChapterId:null, nextChapterId:null,
    contentBlocks:[
      {id:"b1",anchor:"a1",type:"hadith",text:"В чёрном тмине есть исцеление."},
      {id:"b2",anchor:"a2",type:"text",text:"Его также называют шониз."},
    ]
  }],
  topics:[],
  remedies:[{id:"black-seed",title:"Чёрный тмин",aliases:["черный тмин","Nigella","nigella sativa","шониз"],chapterIds:["ch-001"],anchors:["a1"]}],
  stats:{sourceCharacters:0,sourceWords:0,chapters:1,blocks:2,topics:0,remedies:1}
};

describe("searchBook", () => {
  it("treats е and ё as equivalent and ignores case", () => {
    expect(searchBook(book, "ЧЕРНЫЙ ТМИН")[0]?.anchor).toBe("a1");
  });

  it("expands remedy aliases and returns an exact anchor", () => {
    const result = searchBook(book, "Nigella")[0];
    expect(result?.chapterId).toBe("ch-001");
    expect(result?.anchor).toBe("a1");
  });
});