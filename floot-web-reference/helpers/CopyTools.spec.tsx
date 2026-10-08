import { CopyTools } from "./CopyTools";
import { BookModel } from "./BookModel";

describe("CopyTools",()=>{
  it("formats a chapter as clean plain text",()=>{
    const chapter:BookModel.Chapter={
      id:"c1",slug:"test",title:"Глава: Тест",order:1,section:"Раздел",
      topics:[],remedies:[],previousChapterId:null,nextChapterId:null,
      contentBlocks:[
        {id:"b1",anchor:"a1",type:"subheading",text:"## Подзаголовок"},
        {id:"b2",anchor:"a2",type:"text",text:"**Основной** текст."}
      ]
    };
    const text=CopyTools.chapterText(chapter);
    expect(text).toContain("Глава: Тест");
    expect(text).toContain("Подзаголовок");
    expect(text).toContain("Основной текст.");
    expect(text).not.toContain("**");
  });
});