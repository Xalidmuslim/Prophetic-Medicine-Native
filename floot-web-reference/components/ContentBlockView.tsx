import { useState } from "react";
import { Bookmark, Check, Copy } from "lucide-react";
import { Button } from "./Button";
import { BookModel } from "../helpers/BookModel";
import { CopyTools } from "../helpers/CopyTools";
import styles from "./ContentBlockView.module.css";

const clean = (value:string) => value.replace(/^\s*>\s?/gm,"").replace(/\*\*|__/g,"").replace(/\*([^*]+)\*/g,"$1").replace(new RegExp("`([^]+?)`","g"),"$1");

export const ContentBlockView = ({block,showHistoricalLabels,onBookmark}:{block:BookModel.ContentBlock;showHistoricalLabels:boolean;onBookmark:(block:BookModel.ContentBlock)=>void}) => {
  const [copied,setCopied]=useState(false);
  if(block.type==="subheading") {
    const Tag = block.level === 3 ? "h3" : "h2";
    return <Tag id={block.anchor} className={styles.subheading}>{clean(block.text)}</Tag>;
  }
  const copyBlock=async()=>{
    const ok=await CopyTools.copy(CopyTools.blockText(block));
    if(!ok)return;
    setCopied(true);
    window.setTimeout(()=>setCopied(false),1400);
  };
  const label = block.type==="quran" ? "Коран" : block.type==="hadith" ? "Хадис" : block.type==="historical_note" && showHistoricalLabels ? "Медицина эпохи" : null;
  return <div id={block.anchor} data-anchor={block.anchor} className={`${styles.block} ${styles[block.type]}`}>
    {label ? <span className={styles.label}>{label}</span> : null}
    <p>{clean(block.text)}</p>
    <div className={styles.actions}>
      <Button variant="ghost" size="icon-sm" aria-label={copied?"Скопировано":"Копировать абзац"} onClick={copyBlock}>{copied?<Check size={15}/>:<Copy size={15}/>}</Button>
      <Button variant="ghost" size="icon-sm" aria-label="Добавить этот абзац в закладки" onClick={()=>onBookmark(block)}><Bookmark size={15}/></Button>
    </div>
  </div>;
};