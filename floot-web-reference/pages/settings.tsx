import { useState } from "react";
import { Trash2 } from "lucide-react";
import { Button } from "../components/Button";
import { Switch } from "../components/Switch";
import { useThemeMode } from "../helpers/themeMode";
import { Dialog,DialogClose,DialogContent,DialogDescription,DialogFooter,DialogHeader,DialogTitle,DialogTrigger } from "../components/Dialog";
import { PageHeader } from "../components/PageHeader";
import { readerStore } from "../helpers/readerStore";
import styles from "./settings.module.css";

export default function SettingsPage(){
  const theme=useThemeMode();
  const [settings,setSettings]=useState(()=>typeof window==="undefined"?readerStore.defaults.settings:readerStore.load(window.localStorage).settings);
  const save=(patch:Partial<typeof settings>)=>setSettings(readerStore.saveSettings(window.localStorage,patch));
  const setTheme=(mode:"system"|"light"|"dark")=>{save({theme:mode});if(mode==="dark")theme.switchToDarkMode();else if(mode==="light")theme.switchToLightMode();else theme.switchToAutoMode();};
  return <>
    <PageHeader title="Настройки чтения" back/>
    <div className={styles.page}>
      <section className={styles.section}><h2>Оформление</h2><div className={styles.settingRow}><span><strong>Тема приложения</strong><small>Светлая, тёмная или системная</small></span></div><div className={styles.choiceGridThree}>{(["system","light","dark"] as const).map((v,i)=><Button key={v} variant={settings.theme===v?"primary":"outline"} size="sm" onClick={()=>setTheme(v)}>{["Система","Светлая","Тёмная"][i]}</Button>)}</div></section>
      <section className={styles.section}><h2>Размер текста</h2><div className={styles.choiceGrid}>{(["small","medium","large","xlarge"] as const).map((v,i)=><Button key={v} variant={settings.fontSize===v?"primary":"outline"} size="sm" onClick={()=>save({fontSize:v})}>{["Малый","Обычный","Крупный","Очень крупный"][i]}</Button>)}</div></section>
      <section className={styles.section}><h2>Межстрочный интервал</h2><div className={styles.choiceGridThree}>{(["compact","standard","relaxed"] as const).map((v,i)=><Button key={v} variant={settings.lineHeight===v?"primary":"outline"} size="sm" onClick={()=>save({lineHeight:v})}>{["Плотный","Обычный","Свободный"][i]}</Button>)}</div></section>
      <section className={styles.section}><div className={styles.settingRow}><span><strong>Метки медицины эпохи</strong><small>Показывать подпись у исторических медицинских описаний</small></span><Switch checked={settings.showHistoricalLabels} onCheckedChange={(checked)=>save({showHistoricalLabels:checked})}/></div></section>
      <section className={styles.section}><h2>Прогресс</h2><Dialog><DialogTrigger asChild><Button variant="outline"><Trash2 size={16}/> Сбросить прогресс чтения</Button></DialogTrigger><DialogContent><DialogHeader><DialogTitle>Сбросить прогресс?</DialogTitle><DialogDescription>Будут удалены проценты прочтения и история. Закладки и настройки останутся.</DialogDescription></DialogHeader><DialogFooter><DialogClose asChild><Button variant="outline">Отмена</Button></DialogClose><DialogClose asChild><Button variant="destructive" onClick={()=>readerStore.resetProgress(window.localStorage)}>Сбросить</Button></DialogClose></DialogFooter></DialogContent></Dialog></section>
    </div>
  </>;
}