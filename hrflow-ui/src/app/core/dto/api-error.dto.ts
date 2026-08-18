export interface ApiErrorResponse {
    timestamp: string;
    status: string;
    code: string;
    message: string;
    path: string;
}

export interface ApiError {
    status: string;
    code: string;
    message: string;
    path: string;
}

export function toApiError(response: unknown): ApiError | null {
    if (!response || typeof response !== 'object') return null;

    const candidate = response as Partial<ApiErrorResponse>;
    if (candidate.message && candidate.code) {
        return {
            status: candidate.status ?? 'ERROR',
            code: candidate.code,
            message: candidate.message,
            path: candidate.path ?? ''
        };
    }

    return null;
}
