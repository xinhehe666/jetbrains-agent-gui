import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { SettingsTab } from '../SettingsSidebar';
import type {
  OnboardingSetupState,
  OnboardingStepId,
} from '../../../utils/onboardingSetupState';
import styles from './style.module.less';

interface OnboardingSetupCardProps {
  state: OnboardingSetupState;
  onNavigate: (tab: SettingsTab) => void;
}

const DISMISSED_STORAGE_KEY = 'settings.onboardingSetupCard.dismissed';

const SETUP_ITEMS: Array<{
  id: OnboardingStepId;
  label: string;
  target: SettingsTab;
}> = [
  { id: 'node', label: 'Node.js', target: 'basic' },
  { id: 'claude-cli', label: 'Claude CLI', target: 'basic' },
  { id: 'sdk', label: 'Claude SDK', target: 'dependencies' },
];

const OnboardingSetupCard = ({ state, onNavigate }: OnboardingSetupCardProps) => {
  const { t } = useTranslation();
  const [dismissed, setDismissed] = useState(() => {
    try {
      return window.localStorage.getItem(DISMISSED_STORAGE_KEY) === 'true';
    } catch {
      return false;
    }
  });

  const nextItem = useMemo(
    () => SETUP_ITEMS.find((item) => item.id === state.firstActionableStep),
    [state.firstActionableStep],
  );

  if (dismissed || state.complete) {
    return null;
  }

  const handleDismiss = () => {
    setDismissed(true);
    try {
      window.localStorage.setItem(DISMISSED_STORAGE_KEY, 'true');
    } catch {
      // Ignore storage failures; dismissal still applies for this mount.
    }
  };

  return (
    <section className={styles.card} aria-labelledby="onboarding-setup-card-title">
      <div className={styles.header}>
        <div>
          <h2 id="onboarding-setup-card-title" className={styles.title}>
            {t('settings.onboarding.title')}
          </h2>
          <p className={styles.description}>{t('settings.onboarding.description')}</p>
        </div>
        <button
          type="button"
          className={styles.dismissButton}
          aria-label={t('settings.onboarding.action.dismiss')}
          title={t('settings.onboarding.action.dismiss')}
          onClick={handleDismiss}
        >
          <span className="codicon codicon-close" />
        </button>
      </div>

      <div className={styles.checklist}>
        {SETUP_ITEMS.map((item) => {
          const step = state.steps[item.id];
          const status = step.status;
          const isComplete = status === 'complete';
          const isOptional = status === 'optional';
          const statusKey = status === 'complete'
            ? 'settings.onboarding.status.complete'
            : status === 'actionRequired'
              ? 'settings.onboarding.status.actionRequired'
              : status === 'checking'
                ? 'settings.onboarding.status.checking'
                : 'settings.onboarding.status.optional';
          return (
            <div key={item.id} className={`${styles.item} ${isOptional ? styles.optional : ''}`}>
              <span
                className={`codicon ${
                  isComplete
                    ? 'codicon-check'
                    : status === 'checking'
                      ? 'codicon-loading codicon-modifier-spin'
                      : 'codicon-circle-outline'
                } ${styles.statusIcon}`}
              />
              <span className={styles.itemLabel}>{item.label}</span>
              <span className={`${styles.status} ${styles[status]}`}>
                {t(statusKey)}
              </span>
            </div>
          );
        })}
      </div>

      {nextItem && (
        <button type="button" className={styles.nextButton} onClick={() => onNavigate(nextItem.target)}>
          {nextItem.target === 'dependencies'
            ? t('settings.onboarding.action.openDependencies')
            : t('settings.onboarding.action.openEnvironment')}
          <span className="codicon codicon-arrow-right" />
        </button>
      )}
    </section>
  );
};

export default OnboardingSetupCard;
