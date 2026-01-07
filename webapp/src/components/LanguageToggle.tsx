import { useLanguage } from '../contexts/LanguageContext';
import { useTranslation } from '../i18n/useTranslation';

export const LanguageToggle = () => {
  const { language, setLanguage } = useLanguage();
  const { t } = useTranslation();

  const handleLanguageChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setLanguage(e.target.value as 'en-US' | 'es-ES');
  };

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
      <label htmlFor="language-select" style={{ fontSize: '14px', fontWeight: '500', color: 'white' }}>
        {t.nav.language}:
      </label>
      <select
        id="language-select"
        value={language}
        onChange={handleLanguageChange}
        style={{
          padding: '6px 10px',
          fontSize: '14px',
          border: '1px solid #555',
          borderRadius: '4px',
          backgroundColor: '#444',
          color: 'white',
          cursor: 'pointer',
        }}
      >
        <option value="en-US">English</option>
        <option value="es-ES">Español</option>
      </select>
    </div>
  );
};
