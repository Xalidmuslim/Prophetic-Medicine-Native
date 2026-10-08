import { BookModel } from "./BookModel";

const clean = (value:string) =>
  value
    .replace(/^#{1,6}\s+/gm,"")
    .replace(/^\s*>\s?/gm,"")
    .replace(/\*\*|__/g,"")
    .replace(/\*([^*]+)\*/g,"$1")
    .replace(new RegExp("`([^]+?)`","g"),"$1")
    .replace(/\n{3,}/g,"\n\n")
    .trim();

const writeFallback = (text:string) => {
  const area=document.createElement("textarea");
  area.value=text;
  area.setAttribute("readonly","");
  area.style.position="fixed";
  area.style.opacity="0";
  area.style.pointerEvents="none";
  document.body.appendChild(area);
  area.select();
  const ok=document.execCommand?.("copy") ?? false;
  document.body.removeChild(area);
  return ok;
};

export const CopyTools = {
  cleanText(value:string){ return clean(value); },
  blockText(block:BookModel.ContentBlock){ return clean(block.text); },
  chapterText(chapter:BookModel.Chapter){
    return [chapter.title,...chapter.contentBlocks.map((block)=>clean(block.text)).filter(Boolean)].join("\n\n").trim();
  },
  async copy(text:string){
    const value=clean(text);
    if(!value)return false;
    try{
      if(navigator.clipboard?.writeText){ await navigator.clipboard.writeText(value); return true; }
    }catch{ /* use fallback below */ }
    try{return writeFallback(value);}catch{return false;}
  }
};