import { renderHook } from '@testing-library/react';
import { useLoginValidation } from '../useLoginValidation';

describe('useLoginValidation', () => {
  describe('username validation', () => {
    it('should return error when username is empty', () => {
      const { result } = renderHook(() =>
        useLoginValidation('', 'password123', { username: true, password: false })
      );

      expect(result.current.errors.username).toBe('Username is required');
      expect(result.current.isValid).toBe(false);
    });

    it('should return error when username is less than 3 characters', () => {
      const { result } = renderHook(() =>
        useLoginValidation('ab', 'password123', { username: true, password: false })
      );

      expect(result.current.errors.username).toBe('Username must be at least 3 characters');
      expect(result.current.isValid).toBe(false);
    });

    it('should not return error when username is 3 or more characters', () => {
      const { result } = renderHook(() =>
        useLoginValidation('abc', 'password123', { username: true, password: true })
      );

      expect(result.current.errors.username).toBeUndefined();
    });
  });

  describe('password validation', () => {
    it('should return error when password is empty', () => {
      const { result } = renderHook(() =>
        useLoginValidation('username', '', { username: false, password: true })
      );

      expect(result.current.errors.password).toBe('Password is required');
      expect(result.current.isValid).toBe(false);
    });

    it('should return error when password is less than 8 characters', () => {
      const { result } = renderHook(() =>
        useLoginValidation('username', 'pass123', { username: false, password: true })
      );

      expect(result.current.errors.password).toBe('Password must be at least 8 characters');
      expect(result.current.isValid).toBe(false);
    });

    it('should not return error when password is 8 or more characters', () => {
      const { result } = renderHook(() =>
        useLoginValidation('username', 'password123', { username: true, password: true })
      );

      expect(result.current.errors.password).toBeUndefined();
    });
  });

  describe('form validity', () => {
    it('should return isValid as true when both fields are valid', () => {
      const { result } = renderHook(() =>
        useLoginValidation('username', 'password123', { username: true, password: true })
      );

      expect(result.current.isValid).toBe(true);
      expect(result.current.errors).toEqual({});
    });

    it('should return isValid as false when any field is invalid', () => {
      const { result } = renderHook(() =>
        useLoginValidation('ab', 'password123', { username: true, password: true })
      );

      expect(result.current.isValid).toBe(false);
    });
  });

  describe('touched state', () => {
    it('should only show errors for touched fields', () => {
      const { result } = renderHook(() =>
        useLoginValidation('ab', 'pass', { username: true, password: false })
      );

      expect(result.current.touchedErrors.username).toBe('Username must be at least 3 characters');
      expect(result.current.touchedErrors.password).toBeUndefined();
      expect(result.current.errors.username).toBe('Username must be at least 3 characters');
      expect(result.current.errors.password).toBe('Password must be at least 8 characters');
    });

    it('should not show errors for untouched fields', () => {
      const { result } = renderHook(() =>
        useLoginValidation('', '', { username: false, password: false })
      );

      expect(result.current.touchedErrors).toEqual({});
      expect(Object.keys(result.current.errors).length).toBeGreaterThan(0);
    });

    it('should show errors when fields become touched', () => {
      const { result, rerender } = renderHook(
        ({ username, password, touched }) => useLoginValidation(username, password, touched),
        {
          initialProps: {
            username: 'ab',
            password: 'pass',
            touched: { username: false, password: false },
          },
        }
      );

      expect(result.current.touchedErrors).toEqual({});

      rerender({
        username: 'ab',
        password: 'pass',
        touched: { username: true, password: true },
      });

      expect(result.current.touchedErrors.username).toBe('Username must be at least 3 characters');
      expect(result.current.touchedErrors.password).toBe('Password must be at least 8 characters');
    });
  });

  describe('error clearing on correction', () => {
    it('should clear username error when corrected', () => {
      const { result, rerender } = renderHook(
        ({ username, password, touched }) => useLoginValidation(username, password, touched),
        {
          initialProps: {
            username: 'ab',
            password: 'password123',
            touched: { username: true, password: true },
          },
        }
      );

      expect(result.current.errors.username).toBe('Username must be at least 3 characters');

      rerender({
        username: 'abc',
        password: 'password123',
        touched: { username: true, password: true },
      });

      expect(result.current.errors.username).toBeUndefined();
      expect(result.current.isValid).toBe(true);
    });

    it('should clear password error when corrected', () => {
      const { result, rerender } = renderHook(
        ({ username, password, touched }) => useLoginValidation(username, password, touched),
        {
          initialProps: {
            username: 'username',
            password: 'pass',
            touched: { username: true, password: true },
          },
        }
      );

      expect(result.current.errors.password).toBe('Password must be at least 8 characters');

      rerender({
        username: 'username',
        password: 'password123',
        touched: { username: true, password: true },
      });

      expect(result.current.errors.password).toBeUndefined();
      expect(result.current.isValid).toBe(true);
    });
  });
});
