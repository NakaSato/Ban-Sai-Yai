import { apiSlice } from './apiSlice';
import { 
  Member, 
  MemberFormData, 
  MemberStatus, 
  PaginatedResponse, 
  FilterParams,
  ApiResponse 
} from '@/types';

export const membersApi = apiSlice.injectEndpoints({
  endpoints: (builder) => ({
    // Get all members with pagination and filtering
    getMembers: builder.query<PaginatedResponse<Member>, FilterParams>({
      query: (params) => ({
        url: '/members',
        params: {
          page: params.page || 0,
          size: params.size || 10,
          sort: params.sort || 'createdAt',
          order: params.order || 'desc',
          search: params.search,
          status: params.status,
          startDate: params.startDate,
          endDate: params.endDate,
        },
      }),
      providesTags: ['Member'],
    }),

    // Get member by ID
    getMemberById: builder.query<Member, string>({
      query: (id) => `/members/${id}`,
      providesTags: (result, error, id) => [{ type: 'Member', id }],
    }),

    // Create new member
    createMember: builder.mutation<Member, MemberFormData>({
      query: (memberData) => ({
        url: '/members',
        method: 'POST',
        body: memberData,
      }),
      invalidatesTags: ['Member'],
    }),

    // Update member
    updateMember: builder.mutation<Member, { id: string; data: Partial<MemberFormData> }>({
      query: ({ id, data }) => ({
        url: `/members/${id}`,
        method: 'PUT',
        body: data,
      }),
      invalidatesTags: (result, error, { id }) => [{ type: 'Member', id }, 'Member'],
    }),

    // Delete member (soft delete)
    deleteMember: builder.mutation<void, string>({
      query: (id) => ({
        url: `/members/${id}`,
        method: 'DELETE',
      }),
      invalidatesTags: ['Member'],
    }),

    // Activate/Deactivate member
    toggleMemberStatus: builder.mutation<Member, { id: string; status: MemberStatus }>({
      query: ({ id, status }) => ({
        url: `/members/${id}/status`,
        method: 'PATCH',
        body: { status },
      }),
      invalidatesTags: (result, error, { id }) => [{ type: 'Member', id }, 'Member'],
    }),

    // Get member statistics
    getMemberStats: builder.query<{
      totalMembers: number;
      activeMembers: number;
      inactiveMembers: number;
      newMembersThisMonth: number;
      membersByStatus: Record<MemberStatus, number>;
    }, void>({
      query: () => '/members/stats',
      providesTags: ['Dashboard'],
    }),

    // Search members by name or ID
    searchMembers: builder.query<Member[], string>({
      query: (searchTerm) => ({
        url: '/members/search',
        params: { q: searchTerm },
      }),
      providesTags: ['Member'],
    }),

    // Get member's loans
    getMemberLoans: builder.query<any[], string>({
      query: (memberId) => `/members/${memberId}/loans`,
      providesTags: ['Loan'],
    }),

    // Get member's savings accounts
    getMemberSavings: builder.query<any[], string>({
      query: (memberId) => `/members/${memberId}/savings`,
      providesTags: ['SavingAccount'],
    }),

    // Upload member document
    uploadMemberDocument: builder.mutation<{
      id: string;
      filename: string;
      url: string;
    }, { memberId: string; file: File; documentType: string }>({
      query: ({ memberId, file, documentType }) => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('documentType', documentType);
        
        return {
          url: `/members/${memberId}/documents`,
          method: 'POST',
          body: formData,
          formData: true,
        };
      },
      invalidatesTags: (result, error, { memberId }) => [{ type: 'Member', id: memberId }],
    }),

    // Get member documents
    getMemberDocuments: builder.query<any[], string>({
      query: (memberId) => `/members/${memberId}/documents`,
      providesTags: (result, error, memberId) => [{ type: 'Member', id: memberId }],
    }),

    // Delete member document
    deleteMemberDocument: builder.mutation<void, { memberId: string; documentId: string }>({
      query: ({ memberId, documentId }) => ({
        url: `/members/${memberId}/documents/${documentId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (result, error, { memberId }) => [{ type: 'Member', id: memberId }],
    }),
  }),
});

export const {
  useGetMembersQuery,
  useGetMemberByIdQuery,
  useCreateMemberMutation,
  useUpdateMemberMutation,
  useDeleteMemberMutation,
  useToggleMemberStatusMutation,
  useGetMemberStatsQuery,
  useSearchMembersQuery,
  useGetMemberLoansQuery,
  useGetMemberSavingsQuery,
  useUploadMemberDocumentMutation,
  useGetMemberDocumentsQuery,
  useDeleteMemberDocumentMutation,
} = membersApi;
