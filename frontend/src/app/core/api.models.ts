export type TeamMember = 'ALICE' | 'BOB' | 'CHARLIE' | 'DIANA';

export type LeaveStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface LeaveRequest {
  id: number;
  teamMember: TeamMember;
  teamMemberName: string;
  startDate: string;
  endDate: string;
  reason: string;
  status: LeaveStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateLeaveRequest {
  teamMember: TeamMember;
  startDate: string;
  endDate: string;
  reason: string;
}

export interface UpdateLeaveStatusRequest {
  status: LeaveStatus;
}

export interface OnCallWeek {
  weekStart: string;
  weekEnd: string;
  onCallMember: TeamMember;
  onCallMemberName: string;
  conflict: boolean;
  conflictingLeaveRequests: LeaveRequest[];
}
