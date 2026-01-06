import { useLanguage } from '../contexts/LanguageContext';

export const LanguageToggle = () => {
  const { language, setLanguage } = useLanguage();

  const handleLanguageChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setLanguage(e.target.value as 'en-US' | 'es-ES');
  };

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
      <label htmlFor="language-select" style={{ fontSize: '14px', fontWeight: '500', color: 'white' }}>
        Language:
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
