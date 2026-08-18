import { inject, Injectable } from '@angular/core';
import { MessageService } from 'primeng/api';
import { ApiError, toApiError } from '../dto/api-error.dto';

@Injectable({ providedIn: 'root' })
export class ErrorHandlerService {
    private readonly messageService = inject(MessageService);

    handleHttpError(error: unknown, fallbackMessage = 'An unexpected error occurred'): void {
        const apiError = toApiError(error);
        const detail = apiError?.message ?? this.extractMessage(error) ?? fallbackMessage;
        const summary = apiError?.code ?? 'Error';

        this.messageService.add({
            severity: 'error',
            summary,
            detail,
            life: 6000
        });
    }

    handleHttpErrorAndThrow(error: unknown, fallbackMessage = 'An unexpected error occurred'): never {
        this.handleHttpError(error, fallbackMessage);
        throw error;
    }

    showSuccess(summary: string, detail: string): void {
        this.messageService.add({ severity: 'success', summary, detail, life: 4000 });
    }

    showWarning(summary: string, detail: string): void {
        this.messageService.add({ severity: 'warn', summary, detail, life: 5000 });
    }

    showInfo(summary: string, detail: string): void {
        this.messageService.add({ severity: 'info', summary, detail, life: 4000 });
    }

    private extractMessage(error: unknown): string | null {
        if (error instanceof Error) return error.message;
        if (typeof error === 'string') return error;
        if (error && typeof error === 'object' && 'message' in error) {
            return String((error as { message: unknown }).message);
        }
        return null;
    }
}
