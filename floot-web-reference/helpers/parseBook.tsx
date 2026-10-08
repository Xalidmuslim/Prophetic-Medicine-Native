import { BookModel } from "./BookModel";

const translitMap: Record<string, string> = {
  а:"a",б:"b",в:"v",г:"g",д:"d",е:"e",ё:"e",ж:"zh",з:"z",и:"i",й:"y",к:"k",л:"l",м:"m",н:"n",о:"o",п:"p",р:"r",с:"s",т:"t",у:"u",ф:"f",х:"h",ц:"ts",ч:"ch",ш:"sh",щ:"sch",ъ:"",ы:"y",ь:"",э:"e",ю:"yu",я:"ya"
};

const normalize = (value: string) =>
  value.replace(/\u00a0/g, " ").replace(/^\s*>\s?/gm, "").replace(/\*\*|__|\`/g, "").replace(/\s+/g, " ").trim();

const slugify = (value: string) =>
  value.toLocaleLowerCase("ru").split("").map((ch) => translitMap[ch] ?? ch)
    .join("").normalize("NFKD").replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "").slice(0, 84) || "chapter";

const plainSearch = (value: string) => normalize(value).toLocaleLowerCase("ru").replaceAll("ё", "е");

const classify = (text: string, quoted: boolean): BookModel.BlockType => {
  const n = plainSearch(text);
  if (n.startsWith("редакционное примечание") || n.includes("медицинских представлений") || n.includes("медицины xiv века") || n.includes("старой медицины")) return "historical_note";
  if (["(коран,", "всевышний сказал:", "сказал всевышний:", "аллах сказал:"].some((x) => n.includes(x))) return "quran";
  if (["пророк ﷺ сказал", "посланник аллаха ﷺ сказал", "в достоверном хадисе", "в двух «сахихах»", "в «сахихе»", "в сахихе", "от пророка ﷺ"].some((x) => n.includes(x)) || (quoted && text.includes("ﷺ"))) return "hadith";
  return "text";
};

const curated: Array<[string,string,string[]]> = [
  ["black-seed","Чёрный тмин",["черный тмин","чёрное семя","черное семя","nigella","nigella sativa","шониз"]],
  ["costus","Кыст",["куст","къуст","костус","кыст аль-хинди","индийский уд","уд индийский"]],
  ["honey","Мёд",["мед","honey"]],
  ["hijama","Хиджама",["хиджамы","кровопускание"]],
  ["senna","Сенна",["сана"]],
  ["henna","Хна",["henna"]],
  ["zamzam","Замзам",["вода замзам"]],
  ["ajwa-dates","Аджва и финики",["аджва","финик","финики","рутаб","тамр","свежие финики"]],
  ["olive-oil","Оливковое масло",["оливковое масло","олива","масло оливы"]],
  ["ginger","Имбирь",["ginger"]],
  ["barley-talbina","Ячмень и тальбина",["ячмень","ячменя","тальбина"]],
  ["vinegar","Уксус",["уксус"]],
  ["siwak","Сивак",["мисвак"]],
  ["pomegranate","Гранат",["гранат"]],
  ["fig","Инжир",["смоква"]],
  ["grapes-raisins","Виноград и изюм",["виноград","изюм"]],
  ["pumpkin","Тыква",["тыквы","йактин","якътин"]]
];

const topicDefs: Array<[string,string,string[]]> = [
  ["basics","Основы лечения",["основы пророческой медицины","веление лечиться","причин","переедан"]],
  ["diseases","Болезни и их лечение",["лечение ","лечении ","болезн","лихорад","чума","рана","головн","плеврит","водянк","ишиас","глаз"]],
  ["hijama","Хиджама и кровопускание",["хиджам","кровопуск","прижиган"]],
  ["ruqyah","Рукъя и духовное лечение",["рукъ","аль-фатих","духовными божественными"]],
  ["evil-eye","Сглаз и защита",["сглаз","защит","дурного глаза"]],
  ["nutrition","Питание и продукты",["еда","пища","питани","продукт","мёд","мед","финик","ячмен","тальбин","масл","уксус","гранат","инжир","виноград","изюм","тыкв"]],
  ["remedies","Лекарственные средства",["лекарств","средств","мёд","мед","кыст","тмин","сенн","хна","замзам"]],
  ["sleep","Сон и отдых",["сон","бодрствован","бессон"]],
  ["activity","Физическая активность",["физическ","упражнен","движен","ходьб"]],
  ["intimacy","Супружеская близость и телесное здоровье",["половая близость","половая сил","супруж","ишк","влюблён"]],
  ["hygiene","Очищение, гигиена и сивак",["сивак","гигиен","очищен","зуб","омовен"]],
  ["water","Вода и напитки",["вода","питье","питьё","замзам","напит"]],
  ["prevention","Образ жизни и профилактика",["сохранении здоровья","предосторож","воздержан","диет","сон","одежде","жилища","приятного запаха"]],
  ["alphabet","Алфавитный справочник веществ",["алфавитный справочник веществ"]],
  ["conclusion","Заключительные наставления",["заключение"]]
];

export function parseBook(markdown: string): BookModel.Book {
  const chapters: BookModel.Chapter[] = [];
  const slugCounts = new Map<string, number>();
  let current: BookModel.Chapter | null = null;
  let paragraph: string[] = [];
  let paragraphQuoted = false;

  const flush = () => {
    if (!current || !paragraph.length) { paragraph = []; paragraphQuoted = false; return; }
    const raw = paragraph.join("\n").trim();
    if (raw) {
      const idx = current.contentBlocks.length + 1;
      current.contentBlocks.push({ id:`${current.id}-b${String(idx).padStart(4,"0")}`, type:classify(raw, paragraphQuoted), text:raw, anchor:`${current.id}-p${String(idx).padStart(4,"0")}` });
    }
    paragraph = []; paragraphQuoted = false;
  };

  for (const rawLine of markdown.split(/\r?\n/)) {
    const line = rawLine.replace(/\s+$/,"");
    if (line.startsWith("# ")) {
      flush();
      const title = line.slice(2).trim();
      const base = slugify(title);
      const count = (slugCounts.get(base) ?? 0) + 1;
      slugCounts.set(base, count);
      const order = chapters.length + 1;
      current = { id:`ch-${String(order).padStart(3,"0")}`, slug: count === 1 ? base : `${base}-${count}`, title, order, section:"", contentBlocks:[], topics:[], remedies:[], previousChapterId:null, nextChapterId:null };
      chapters.push(current);
      continue;
    }
    if (!current) continue;
    if (/^##+\s/.test(line)) {
      flush();
      const level = Math.min(3, line.match(/^#+/)?.[0].length ?? 2);
      const heading = line.replace(/^##+\s*/, "").trim();
      if (heading) {
        const idx = current.contentBlocks.length + 1;
        current.contentBlocks.push({ id:`${current.id}-b${String(idx).padStart(4,"0")}`, type:"subheading", text:heading, anchor:`${current.id}-p${String(idx).padStart(4,"0")}`, level });
      }
      continue;
    }
    if (line.trim() === "---" || !line.trim()) { flush(); continue; }
    if (line.trimStart().startsWith(">")) paragraphQuoted = true;
    paragraph.push(line);
  }
  flush();

  let section = "Основы пророческой медицины";
  for (const ch of chapters) {
    const t = ch.title.toLocaleLowerCase("ru");
    if (t.includes("разделы о лечении больных") || t.includes("о лечении больных пророком")) section = "Естественные средства и телесное лечение";
    if (t.includes("разделы о лечении духовными")) section = "Рукъя и духовное лечение";
    if (t === "глава: руководство пророка ﷺ о сохранении здоровья") section = "Сохранение здоровья и образ жизни";
    if (t.includes("некоторые простые лекарства и продукты")) section = "Алфавитный справочник веществ";
    if (t.startsWith("заключительная глава")) section = "Заключение";
    if (t.startsWith("ссылки на аяты")) section = "Приложение";
    ch.section = section;
  }
  chapters.forEach((ch,i) => { ch.previousChapterId = chapters[i-1]?.id ?? null; ch.nextChapterId = chapters[i+1]?.id ?? null; });

  const blob = (ch: BookModel.Chapter) => plainSearch(ch.title + "\n" + ch.contentBlocks.map((b) => b.text).join("\n"));
  const topics: BookModel.Topic[] = topicDefs.map(([id,title,keywords]) => {
    const chapterIds = chapters.filter((ch) => {
      const text = blob(ch);
      let match = false;
      if (id === "alphabet") match = ch.section === "Алфавитный справочник веществ";
      else if (id === "conclusion") match = ch.section === "Заключение" || ch.section === "Приложение";
      else if (id === "basics") match = ch.section === "Основы пророческой медицины" || keywords.some((k) => text.includes(k.replaceAll("ё","е")));
      else match = keywords.some((k) => text.includes(k.replaceAll("ё","е")));
      if (match) ch.topics.push(id);
      return match;
    }).map((ch) => ch.id);
    return {id,title,chapterIds};
  });

  const remedies: BookModel.Remedy[] = [];
  const seen = new Set<string>();
  const addRemedy = (id:string,title:string,aliases:string[]) => {
    const key = plainSearch(title); if (!key || seen.has(key)) return;
    const names = [title,...aliases].map(plainSearch).filter(Boolean);
    const chapterIds:string[] = []; const anchors:string[] = [];
    for (const ch of chapters) {
      const found = ch.contentBlocks.find((b) => names.some((n) => plainSearch(b.text).includes(n)));
      if (found || names.some((n) => plainSearch(ch.title).includes(n))) {
        chapterIds.push(ch.id); anchors.push(found?.anchor ?? ch.contentBlocks[0]?.anchor ?? "");
      }
    }
    if (!chapterIds.length) return;
    seen.add(key); remedies.push({id,title,aliases:[...new Set(aliases)],chapterIds,anchors});
    chapters.filter((ch) => chapterIds.includes(ch.id)).forEach((ch) => ch.remedies.push(id));
  };
  curated.forEach(([id,title,aliases]) => addRemedy(id,title,aliases));
  for (const ch of chapters) {
    if (ch.section !== "Алфавитный справочник веществ") continue;
    for (const block of ch.contentBlocks) {
      if (block.type !== "subheading" || block.level !== 2) continue;
      const heading = normalize(block.text);
      if (!heading || ["раздел","примечание"].includes(heading.toLocaleLowerCase("ru"))) continue;
      const parts = heading.split(/\s+[—–-]\s+|\s*\/\s*/).map((x) => x.trim()).filter(Boolean);
      if ((parts[0]?.length ?? 0) < 2) continue;
      addRemedy("ref-" + slugify(parts[0]), parts[0], parts.slice(1));
    }
  }
  remedies.sort((a,b) => plainSearch(a.title).localeCompare(plainSearch(b.title),"ru"));
  return { title:"Медицина Пророка ﷺ", author:"Ибн Каййим аль-Джаузия", chapters, topics, remedies, stats:{ sourceCharacters:markdown.length, sourceWords:markdown.trim().split(/\s+/).length, chapters:chapters.length, blocks:chapters.reduce((n,ch)=>n+ch.contentBlocks.length,0), topics:topics.length, remedies:remedies.length } };
}