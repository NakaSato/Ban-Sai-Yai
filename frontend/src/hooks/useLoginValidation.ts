import { useMemo } from 'react';

interface ValidationErrors {
  username?: string;
  password?: string;
}

interface TouchedFields {
  username: boolean;
  password: boolean;
}

interface ValidationResult {
  errors: ValidationErrors;
  isValid: boolean;
  touchedErrors: ValidationErrors;
}

/**
 * Custom hook for validating login form inputs
 * 
 * @param username - The username input value
 * @param password - The password input value
 * @param touched - Object tracking which fields have been interacted with
 * @returns Validation result containing errors, validity state, and touched errors
 */
export const useLoginValidation = (
  username: string,
  password: string,
  touched: TouchedFields
): ValidationResult => {
  const errors = useMemo(() => {
    const validationErrors: ValidationErrors = {};

    // Username validation
    if (!username) {
      validationErrors.username = 'Username is required';
    } else if (username.length < 3) {
      validationErrors.username = 'Username must be at least 3 characters';
    }

    // Password validation
    if (!password) {
      validationErrors.password = 'Password is required';
    } else if (password.length < 8) {
      validationErrors.password = 'Password must be at least 8 characters';
    }

    return validationErrors;
  }, [username, password]);

  // Only show errors for fields that have been touched
  const touchedErrors = useMemo(() => {
    const result: ValidationErrors = {};
    
    if (touched.username && errors.username) {
      result.username = errors.username;
    }
    
    if (touched.password && errors.password) {
      result.password = errors.password;
    }
    
    return result;
  }, [errors, touched]);

  // Form is valid if there are no validation errors
  const isValid = useMemo(() => {
    return Object.keys(errors).length === 0;
  }, [errors]);

  return {
    errors,
    isValid,
    touchedErrors,
  };
};

export default useLoginValidation;
