import { buildTreatmentIndex } from "./buildTreatmentIndex";
import { BookModel } from "./BookModel";

const book: BookModel.Book = {
  title:"Тест",author:"Автор",
  chapters:[
    {id:"c1",slug:"fever",title:"Глава: Руководство Пророка ﷺ в лечении лихорадки",order:1,section:"Естественные средства",contentBlocks:[],topics:[],remedies:["honey"],previousChapterId:null,nextChapterId:"c2"},
    {id:"c2",slug:"food",title:"Глава: Руководство Пророка ﷺ в еде и питье",order:2,section:"Образ жизни",contentBlocks:[],topics:[],remedies:[],previousChapterId:"c1",nextChapterId:null}
  ],
  topics:[],
  remedies:[{id:"honey",title:"Мёд",aliases:[],chapterIds:["c1"],anchors:["a1"]}],
  stats:{sourceCharacters:0,sourceWords:0,chapters:2,blocks:0,topics:0,remedies:1}
};

describe("buildTreatmentIndex",()=>{
  it("includes treatment chapters and exposes mentioned remedies",()=>{
    const items=buildTreatmentIndex(book);
    expect(items.some((item)=>item.chapterSlug==="fever")).toBe(true);
    expect(items.find((item)=>item.chapterSlug==="fever")?.remedies[0]?.title).toBe("Мёд");
  });
  it("does not turn general food guidance into a disease treatment entry",()=>{
    expect(buildTreatmentIndex(book).some((item)=>item.chapterSlug==="food")).toBe(false);
  });
});