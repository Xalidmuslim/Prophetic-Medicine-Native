import { BookModel } from "./BookModel";

export interface QuickCollection {
  id: string;
  title: string;
  description: string;
  keywords: string[];
  chapterIds: string[];
}

const defs: Array<Omit<QuickCollection,"chapterIds">> = [
  {
    id:"headache",
    title:"Головная боль и мигрень",
    description:"Главы о головной боли, мигрени и связанных способах лечения.",
    keywords:["головной боли","мигрени","причина мигрени"]
  },
  {
    id:"digestion",
    title:"Желудок и пищеварение",
    description:"Понос, запор, рвота, питание больного и лечебное воздержание.",
    keywords:["поноса","запора","размягчить кишечник","очищении посредством рвоты","питании больного","лечебного воздержания","диеты"]
  },
  {
    id:"sleep",
    title:"Сон и бессонница",
    description:"Сон, бодрствование, страх и бессонница.",
    keywords:["сна и бодрствования","страха и бессонницы"]
  },
  {
    id:"anxiety",
    title:"Тревога, печаль и душевное стеснение",
    description:"Главы о тревоге, тоске, печали, испытаниях и состояниях сердца.",
    keywords:["тревоги, тоски, печали","жара беды и печали","болезни сердца","чрезмерной влюблённости","чрезмерной влюбленности"]
  },
  {
    id:"ruqyah",
    title:"Рукъя, сглаз и защита",
    description:"Сглаз, рукъя, аль-Фатиха, укусы и защитные чтения.",
    keywords:["сглазом","сглазить","рукъ","аль-фатихи","укуса скорпиона","укусе змеи","ужаленного"]
  },
  {
    id:"hijama",
    title:"Хиджама",
    description:"Польза, места, время и правовые выводы, связанные с хиджамой.",
    keywords:["хиджам","вскрытия сосудов"]
  },
  {
    id:"eyes",
    title:"Глаза",
    description:"Воспаление глаз и сохранение здоровья зрения.",
    keywords:["воспаления глаз","здоровья глаз"]
  },
  {
    id:"skin-wounds",
    title:"Раны, кожа и нарывы",
    description:"Раны, язвы, зуд, вши, гнойнички, опухоли и нарывы.",
    keywords:["лечении раны","язве и ране","кожном зуде","головных вшей","гнойничка","опухолей и нарывов"]
  }
];

const normalize=(value:string)=>value.toLocaleLowerCase("ru").replaceAll("ё","е");

export function buildQuickCollections(book:BookModel.Book):QuickCollection[]{
  return defs.map((def)=>{
    const chapterIds=book.chapters
      .filter((chapter)=>{
        const title=normalize(chapter.title);
        return def.keywords.some((keyword)=>title.includes(normalize(keyword)));
      })
      .map((chapter)=>chapter.id);
    return {...def,chapterIds};
  });
}