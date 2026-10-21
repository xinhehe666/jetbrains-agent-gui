import { describe, expect, it } from 'vitest';
import {
  deriveOnboardingSetupState,
  ONBOARDING_STEP_IDS,
  type OnboardingSetupInput,
} from './onboardingSetupState';

describe('deriveOnboardingSetupState', () => {
  it('normalizes the native node, CLI, and SDK payload shapes', () => {
    expect(deriveOnboardingSetupState({
      node: { available: true },
      claudeCli: { installed: true },
      sdk: { 'claude-sdk': { status: 'installed' } },
    })).toEqual({
      status: 'complete',
      ready: true,
      actionRequired: false,
      complete: true,
      firstActionableStep: null,
      steps: {
        node: { status: 'complete', required: true },
        'claude-cli': { status: 'complete', required: false },
        sdk: { status: 'complete', required: true },
      },
    });
  });

  it('finds the first actionable step in stable onboarding order', () => {
    expect(deriveOnboardingSetupState({
      node: { available: true },
      claudeCli: { installed: false },
      sdk: { status: 'installed' },
    })).toEqual({
      status: 'actionRequired',
      ready: false,
      actionRequired: true,
      complete: false,
      firstActionableStep: 'claude-cli',
      steps: {
        node: { status: 'complete', required: true },
        'claude-cli': { status: 'actionRequired', required: false },
        sdk: { status: 'complete', required: true },
      },
    });
  });

  it('keeps missing required steps actionable and optional CLI setup optional', () => {
    const input: OnboardingSetupInput = {
      node: { available: 'yes' },
      claudeCli: { status: 'pending' },
      sdk: { 'claude-sdk': { status: null } },
    };
    const before = JSON.stringify(input);

    expect(deriveOnboardingSetupState(input)).toEqual({
      status: 'actionRequired',
      ready: false,
      actionRequired: true,
      complete: false,
      firstActionableStep: 'node',
      steps: {
        node: { status: 'actionRequired', required: true },
        'claude-cli': { status: 'checking', required: false },
        sdk: { status: 'actionRequired', required: true },
      },
    });
    expect(JSON.stringify(input)).toBe(before);
  });

  it('treats a missing Claude CLI override as optional and complete', () => {
    expect(deriveOnboardingSetupState({
      node: { available: true },
      sdk: { installed: true },
    })).toMatchObject({
      status: 'complete',
      ready: true,
      complete: true,
      firstActionableStep: null,
      steps: {
        'claude-cli': { status: 'optional', required: false },
      },
    });
  });

  it('reports checking instead of actionable while required probes are pending', () => {
    expect(deriveOnboardingSetupState({
      node: { status: 'checking' },
      sdk: { installed: true },
    })).toMatchObject({
      status: 'checking',
      ready: false,
      actionRequired: false,
      complete: false,
      firstActionableStep: null,
      steps: {
        node: { status: 'checking', required: true },
      },
    });
  });

  it('accepts nullish and malformed input and always returns JSON-safe state', () => {
    for (const input of [undefined, null, 'unexpected', 42]) {
      const state = deriveOnboardingSetupState(input);
      expect(state).toEqual({
        status: 'actionRequired',
        ready: false,
        actionRequired: true,
        complete: false,
        firstActionableStep: 'node',
        steps: {
          node: { status: 'actionRequired', required: true },
          'claude-cli': { status: 'optional', required: false },
          sdk: { status: 'actionRequired', required: true },
        },
      });
      expect(JSON.parse(JSON.stringify(state))).toEqual(state);
    }
    expect(ONBOARDING_STEP_IDS).toEqual(['node', 'claude-cli', 'sdk']);
  });
});
