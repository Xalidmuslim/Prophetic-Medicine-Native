import { buildQuickCollections } from "./buildQuickCollections";
import { BookModel } from "./BookModel";

const book: BookModel.Book = {
  title:"Тест",author:"Автор",
  chapters:[
    {id:"c1",slug:"headache",title:"Глава: Лечение мигрени и головной боли",order:1,section:"Телесные состояния",contentBlocks:[],topics:[],remedies:[],previousChapterId:null,nextChapterId:"c2"},
    {id:"c2",slug:"sleep",title:"Глава: Руководство Пророка ﷺ о лечении страха и бессонницы",order:2,section:"Рукъя",contentBlocks:[],topics:[],remedies:[],previousChapterId:"c1",nextChapterId:null}
  ],
  topics:[],remedies:[],stats:{sourceCharacters:0,sourceWords:0,chapters:2,blocks:0,topics:0,remedies:0}
};

describe("buildQuickCollections",()=>{
  it("maps headache collection to headache chapter",()=>{
    const c=buildQuickCollections(book).find((x)=>x.id==="headache");
    expect(c?.chapterIds).toContain("c1");
  });
  it("maps sleep collection to insomnia chapter",()=>{
    const c=buildQuickCollections(book).find((x)=>x.id==="sleep");
    expect(c?.chapterIds).toContain("c2");
  });
});