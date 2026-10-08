import { BookModel } from "./BookModel";

export interface TreatmentIndexItem {
  id: string;
  condition: string;
  chapterId: string;
  chapterSlug: string;
  chapterTitle: string;
  section: string;
  category: "Телесные состояния" | "Рукъя и защита" | "Состояния сердца" | "Предосторожность и поддержка";
  remedies: BookModel.Remedy[];
  methods: string[];
}

const normalize=(value:string)=>value.toLocaleLowerCase("ru").replaceAll("ё","е");

const exclusions=[
  "веление лечиться",
  "о лечении больных пророком",
  "лечение естественными средствами",
  "ответственности того, кто лечит",
  "более искусному из двух врачей",
  "запрет лечиться запретным",
];

const isTreatmentChapter=(title:string)=>{
  const t=normalize(title);
  if(exclusions.some((x)=>t.includes(x))) return false;
  return [
    "в лечении ",
    "при водянке",
    "лечении раны",
    "лечении припадков",
    "лечении ишиаса",
    "лечении запора",
    "кожном зуде",
    "лечении плеврита",
    "головной боли",
    "мигрени",
    "воспаления горла",
    "болезни сердца",
    "воспаления глаз",
    "общем онемении",
    "гнойничка",
    "опухолей и нарывов",
    "очищении посредством рвоты",
    "заразных болезнях",
    "головных вшей",
    "пораженного сглазом",
    "лечения сглаза",
    "сглазить другого",
    "рукъ",
    "укуса ",
    "ужаленного",
    "язве и ране",
    "лечении боли",
    "беды и печали",
    "тревоги",
    "страха и бессонницы",
    "чрезмерной влюбленности",
  ].some((needle)=>t.includes(needle));
};

const getCategory=(title:string):TreatmentIndexItem["category"]=>{
  const t=normalize(title);
  if(["сглаз","рукъ","укуса скорпиона","укусе змеи","ужаленного"].some((x)=>t.includes(x))) return "Рукъя и защита";
  if(["тревоги","печали","страха","влюбленности","душевного"].some((x)=>t.includes(x))) return "Состояния сердца";
  if(["заразных","предосторож","питании больного","не заставлять больного","диет"].some((x)=>t.includes(x))) return "Предосторожность и поддержка";
  return "Телесные состояния";
};

const conditionFromTitle=(title:string)=>{
  let value=title
    .replace(/^Глава:\s*/i,"")
    .replace(/^Руководство Пророка ﷺ\s*/i,"")
    .replace(/^в лечении\s+/i,"")
    .replace(/^о лечении\s+/i,"")
    .replace(/^при\s+/i,"")
    .replace(/^относительно\s+/i,"")
    .trim();
  value=value.charAt(0).toUpperCase()+value.slice(1);
  return value;
};

const methodRules:Array<[string,string]>= [
  ["хиджам","Хиджама"],
  ["прижиган","Прижигание"],
  ["мед","Мёд"],
  ["мёд","Мёд"],
  ["кыст","Кыст"],
  ["куст","Кыст"],
  ["черн","Чёрный тмин"],
  ["тмин","Чёрный тмин"],
  ["хна","Хна"],
  ["сенн","Сенна"],
  ["рукъ","Рукъя"],
  ["аль-фатих","Аль-Фатиха"],
  ["омовен","Омовение"],
  ["мольб","Мольба"],
  ["дуа","Мольба"],
  ["диет","Воздержание / диета"],
  ["поко","Покой"],
  ["рвот","Рвота"],
  ["вскрыт","Вскрытие / дренирование"],
  ["дренирован","Вскрытие / дренирование"],
  ["вод","Вода"],
];

export function buildTreatmentIndex(book:BookModel.Book):TreatmentIndexItem[]{
  return book.chapters
    .filter((chapter)=>isTreatmentChapter(chapter.title))
    .map((chapter)=>{
      const remedies=chapter.remedies
        .map((id)=>book.remedies.find((r)=>r.id===id))
        .filter((r):r is BookModel.Remedy=>Boolean(r))
        .slice(0,8);
      const searchable=normalize(chapter.title+" "+chapter.contentBlocks.slice(0,12).map((b)=>b.text).join(" "));
      const methods=[...new Set(methodRules.filter(([needle])=>searchable.includes(needle)).map(([,label])=>label))].slice(0,6);
      return {
        id:"tx-"+chapter.id,
        condition:conditionFromTitle(chapter.title),
        chapterId:chapter.id,
        chapterSlug:chapter.slug,
        chapterTitle:chapter.title,
        section:chapter.section,
        category:getCategory(chapter.title),
        remedies,
        methods,
      };
    });
}