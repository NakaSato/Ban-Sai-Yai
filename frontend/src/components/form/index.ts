/**
 * Form Components
 *
 * A collection of reusable form components integrated with react-hook-form
 * and MUI, designed for the Ban Sai Yai Savings Group application.
 *
 * Usage:
 * ```tsx
 * import { FormProvider, useForm } from 'react-hook-form';
 * import { yupResolver } from '@hookform/resolvers/yup';
 * import { FormInput, FormSelect, FormDatePicker } from '@/components/form';
 * import { memberSchema } from '@/utils/schemas';
 *
 * function MemberForm() {
 *   const methods = useForm({
 *     resolver: yupResolver(memberSchema),
 *     defaultValues: { firstName: '', lastName: '' }
 *   });
 *
 *   return (
 *     <FormProvider {...methods}>
 *       <form onSubmit={methods.handleSubmit(onSubmit)}>
 *         <FormInput name="firstName" label="ชื่อ" required />
 *         <FormInput name="lastName" label="นามสกุล" required />
 *         <FormSelect
 *           name="status"
 *           label="สถานะ"
 *           options={[
 *             { value: 'ACTIVE', label: 'ใช้งาน' },
 *             { value: 'INACTIVE', label: 'ไม่ใช้งาน' },
 *           ]}
 *         />
 *       </form>
 *     </FormProvider>
 *   );
 * }
 * ```
 */

// Text input components
export { FormInput, FormPasswordInput } from "./FormInput";

// Select components
export { FormSelect, FormAutocomplete } from "./FormSelect";

// Checkbox, switch, and radio components
export {
  FormCheckbox,
  FormSwitch,
  FormRadioGroup,
  FormCheckboxGroup,
} from "./FormCheckbox";

// Date/Time picker components
export {
  FormDatePicker,
  FormDateTimePicker,
  FormTimePicker,
  FormDateRangePicker,
} from "./FormDatePicker";

// Textarea and numeric input components
export {
  FormTextarea,
  FormCurrencyInput,
  FormNumberInput,
  FormPercentageInput,
} from "./FormTextarea";
