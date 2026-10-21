import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import OnboardingSetupCard from './index';
import { deriveOnboardingSetupState } from '../../../utils/onboardingSetupState';

const translations: Record<string, string> = {
  'settings.onboarding.title': 'Complete your setup',
  'settings.onboarding.description': 'Complete these steps to get started with AI features.',
  'settings.onboarding.status.complete': 'Complete',
  'settings.onboarding.status.actionRequired': 'Action required',
  'settings.onboarding.status.checking': 'Checking',
  'settings.onboarding.status.optional': 'Optional',
  'settings.onboarding.action.openEnvironment': 'Open Environment Settings',
  'settings.onboarding.action.openDependencies': 'Open SDK Dependencies',
  'settings.onboarding.action.dismiss': 'Dismiss',
};

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => translations[key] ?? key,
  }),
}));

const DISMISSED_STORAGE_KEY = 'settings.onboardingSetupCard.dismissed';

describe('OnboardingSetupCard', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  it('renders required and optional steps from the production setup-state model', () => {
    render(
      <OnboardingSetupCard
        state={deriveOnboardingSetupState({
          node: { installed: true },
          claudeCli: { status: 'optional' },
          sdk: { installed: false },
        })}
        onNavigate={vi.fn()}
      />,
    );

    expect(screen.getByRole('heading', { name: 'Complete your setup' })).toBeTruthy();
    expect(screen.getByText('Node.js')).toBeTruthy();
    expect(screen.getByText('Claude CLI')).toBeTruthy();
    expect(screen.getByText('Claude SDK')).toBeTruthy();
    expect(screen.getAllByText('Complete')).toHaveLength(1);
    expect(screen.getByText('Optional')).toBeTruthy();
    expect(screen.getByText('Action required')).toBeTruthy();
  });

  it('opens the first actionable destination', () => {
    const onNavigate = vi.fn();

    render(
      <OnboardingSetupCard
        state={deriveOnboardingSetupState({
          node: { installed: true },
          sdk: { installed: false },
        })}
        onNavigate={onNavigate}
      />,
    );

    fireEvent.click(screen.getByRole('button', { name: 'Open SDK Dependencies' }));

    expect(onNavigate).toHaveBeenCalledWith('dependencies');
  });

  it('renders a checking state without an action button', () => {
    render(
      <OnboardingSetupCard
        state={deriveOnboardingSetupState({
          node: { status: 'checking' },
          sdk: { installed: true },
        })}
        onNavigate={vi.fn()}
      />,
    );

    expect(screen.getByText('Checking')).toBeTruthy();
    expect(screen.queryByRole('button', { name: 'Open SDK Dependencies' })).toBeNull();
    expect(screen.queryByRole('button', { name: 'Open Environment Settings' })).toBeNull();
  });

  it('hides itself when all required setup is complete', () => {
    const { container } = render(
      <OnboardingSetupCard
        state={deriveOnboardingSetupState({
          node: { installed: true },
          sdk: { installed: true },
        })}
        onNavigate={vi.fn()}
      />,
    );

    expect(container.firstChild).toBeNull();
  });

  it('persists dismissal and stays hidden after remount', () => {
    const state = deriveOnboardingSetupState({
      node: { installed: false },
      sdk: { installed: false },
    });
    const { unmount } = render(
      <OnboardingSetupCard state={state} onNavigate={vi.fn()} />,
    );

    fireEvent.click(screen.getByRole('button', { name: 'Dismiss' }));

    expect(screen.queryByRole('heading', { name: 'Complete your setup' })).toBeNull();
    expect(localStorage.getItem(DISMISSED_STORAGE_KEY)).toBe('true');

    unmount();
    render(<OnboardingSetupCard state={state} onNavigate={vi.fn()} />);
    expect(screen.queryByRole('heading', { name: 'Complete your setup' })).toBeNull();
  });
});
