import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';

export type Language = 'en-US' | 'es-ES';

interface LanguageContextType {
  language: Language;
  setLanguage: (language: Language) => void;
  toggleLanguage: () => void;
}

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

const LANGUAGE_KEY = 'app-language';

export const LanguageProvider = ({ children }: { children: ReactNode }) => {
  const [language, setLanguageState] = useState<Language>(() => {
    const stored = localStorage.getItem(LANGUAGE_KEY);
    if (stored === 'en-US' || stored === 'es-ES') {
      return stored;
    }
    return 'en-US';
  });

  useEffect(() => {
    localStorage.setItem(LANGUAGE_KEY, language);

    window.dispatchEvent(new CustomEvent('languageChange', { detail: language }));
  }, [language]);

  const setLanguage = (newLanguage: Language) => {
    setLanguageState(newLanguage);
  };

  const toggleLanguage = () => {
    setLanguageState((prev) => (prev === 'en-US' ? 'es-ES' : 'en-US'));
  };

  return (
    <LanguageContext.Provider value={{ language, setLanguage, toggleLanguage }}>
      {children}
    </LanguageContext.Provider>
  );
};

export const useLanguage = () => {
  const context = useContext(LanguageContext);
  if (context === undefined) {
    throw new Error('useLanguage must be used within a LanguageProvider');
  }
  return context;
};
