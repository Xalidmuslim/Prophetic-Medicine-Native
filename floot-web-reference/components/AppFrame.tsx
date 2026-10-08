import React, { useEffect } from "react";
import { NavLink } from "react-router-dom";
import { readerStore } from "../helpers/readerStore";
import { switchToAutoMode, switchToDarkMode, switchToLightMode } from "../helpers/themeMode";
import { Bookmark, Home, LayoutGrid, MoreHorizontal, Search } from "lucide-react";
import styles from "./AppFrame.module.css";

const nav = [
  {to:"/",label:"Главная",Icon:Home,end:true},
  {to:"/topics",label:"Темы",Icon:LayoutGrid,end:false},
  {to:"/search",label:"Поиск",Icon:Search,end:false},
  {to:"/bookmarks",label:"Закладки",Icon:Bookmark,end:false},
  {to:"/more",label:"Ещё",Icon:MoreHorizontal,end:false},
];

export const AppFrame = ({children}:{children:React.ReactNode}) => {
  useEffect(() => {
    const mode = readerStore.load(window.localStorage).settings.theme;
    if (mode === "dark") switchToDarkMode();
    else if (mode === "light") switchToLightMode();
    else switchToAutoMode();
  }, []);
  return <div className={styles.frame}>
    <main className={styles.main}>{children}</main>
    <nav className={styles.nav} aria-label="Основная навигация">
      <div className={styles.navInner}>
        {nav.map(({to,label,Icon,end}) => (
          <NavLink key={to} to={to} end={end} className={({isActive}) => `${styles.navItem} ${isActive ? styles.active : ""}`}>
            <Icon size={21} strokeWidth={1.8} aria-hidden="true"/>
            <span>{label}</span>
          </NavLink>
        ))}
      </div>
    </nav>
  </div>;
};