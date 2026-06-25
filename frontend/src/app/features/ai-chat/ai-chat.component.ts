import { Component, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AiService, ChatResponse } from '@core/services/ai.service';

interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  sourceDocs?: string[];
  timestamp: Date;
}

@Component({
  selector: 'app-ai-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <div class="chat-page">
      <div class="chat-container">
        <!-- Header -->
        <div class="chat-header">
          <div class="header-left">
            <div class="ai-avatar">
              <mat-icon>auto_awesome</mat-icon>
              <div class="pulse-ring"></div>
            </div>
            <div>
              <h2>AI Claims Assistant</h2>
              <span class="status-online"><span class="online-dot"></span> Powered by Claude AI</span>
            </div>
          </div>
        </div>

        <!-- Messages -->
        <div class="messages-area" #messagesArea>
          <!-- Welcome -->
          <div class="welcome" *ngIf="messages.length === 0">
            <div class="welcome-icon">
              <mat-icon>auto_awesome</mat-icon>
            </div>
            <h3>How can I help you today?</h3>
            <p>Ask me about claims, policies, fraud analysis, or documents</p>

            <div class="suggestions">
              <button class="suggestion" *ngFor="let s of suggestions" (click)="useSuggestion(s)">
                <mat-icon>{{ s.icon }}</mat-icon>
                <span>{{ s.text }}</span>
              </button>
            </div>
          </div>

          <!-- Chat Messages -->
          <div *ngFor="let msg of messages" [class]="'message ' + msg.role">
            <div class="msg-avatar" *ngIf="msg.role === 'assistant'">
              <mat-icon>auto_awesome</mat-icon>
            </div>
            <div class="msg-bubble">
              <div class="msg-content">{{ msg.content }}</div>
              <div *ngIf="msg.sourceDocs && msg.sourceDocs.length > 0" class="msg-sources">
                <mat-icon>source</mat-icon>
                <span>Sources: {{ msg.sourceDocs!.join(', ') }}</span>
              </div>
              <div class="msg-time">{{ msg.timestamp | date:'shortTime' }}</div>
            </div>
            <div class="msg-avatar user-avatar" *ngIf="msg.role === 'user'">
              <mat-icon>person</mat-icon>
            </div>
          </div>

          <!-- Typing Indicator -->
          <div class="message assistant" *ngIf="loading">
            <div class="msg-avatar"><mat-icon>auto_awesome</mat-icon></div>
            <div class="msg-bubble typing-bubble">
              <div class="typing-dots">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- Input -->
        <div class="input-area">
          <div class="input-box">
            <input [(ngModel)]="userMessage"
                   (keyup.enter)="sendMessage()"
                   [disabled]="loading"
                   placeholder="Ask about a claim, policy, or document...">
            <button class="send-btn" (click)="sendMessage()"
                    [disabled]="!userMessage.trim() || loading"
                    [class.active]="userMessage.trim()">
              <mat-icon>arrow_upward</mat-icon>
            </button>
          </div>
          <p class="disclaimer">AI responses are for reference only. Always verify critical decisions.</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .chat-page {
      height: calc(100vh - 64px); display: flex; justify-content: center;
      padding: 24px; background: transparent;
    }
    .chat-container {
      width: 100%; max-width: 860px; display: flex; flex-direction: column;
      background: rgba(255,255,255,0.02);
      border: 1px solid rgba(255,255,255,0.06);
      border-radius: 20px; overflow: hidden;
    }

    /* Header */
    .chat-header {
      padding: 16px 24px; display: flex; justify-content: space-between; align-items: center;
      border-bottom: 1px solid rgba(255,255,255,0.06);
      background: rgba(255,255,255,0.02);
    }
    .header-left { display: flex; align-items: center; gap: 12px; }
    .ai-avatar {
      position: relative; width: 40px; height: 40px; border-radius: 12px;
      background: linear-gradient(135deg, #8b5cf6, #3b82f6);
      display: flex; align-items: center; justify-content: center;
    }
    .ai-avatar mat-icon { color: white; font-size: 20px; width: 20px; height: 20px; }
    .pulse-ring {
      position: absolute; inset: -4px; border-radius: 16px;
      border: 2px solid rgba(139, 92, 246, 0.3);
      animation: pulseRing 2s ease-out infinite;
    }
    @keyframes pulseRing { 0% { transform: scale(1); opacity: 1; } 100% { transform: scale(1.3); opacity: 0; } }
    .chat-header h2 { font-size: 15px; font-weight: 600; }
    .status-online { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #10b981; }
    .online-dot { width: 6px; height: 6px; border-radius: 50%; background: #10b981; }

    /* Messages */
    .messages-area {
      flex: 1; overflow-y: auto; padding: 24px;
      display: flex; flex-direction: column; gap: 16px;
    }

    /* Welcome */
    .welcome { text-align: center; padding: 60px 0 40px; }
    .welcome-icon {
      width: 64px; height: 64px; border-radius: 20px; margin: 0 auto 20px;
      background: linear-gradient(135deg, rgba(139, 92, 246, 0.15), rgba(59, 130, 246, 0.15));
      border: 1px solid rgba(139, 92, 246, 0.2);
      display: flex; align-items: center; justify-content: center;
    }
    .welcome-icon mat-icon { color: #8b5cf6; font-size: 28px; width: 28px; height: 28px; }
    .welcome h3 { font-size: 22px; font-weight: 700; margin-bottom: 8px; }
    .welcome p { color: #64748b; font-size: 14px; }

    .suggestions { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; margin-top: 32px; }
    .suggestion {
      display: flex; align-items: center; gap: 6px;
      padding: 10px 16px; border-radius: 12px;
      background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06);
      color: #94a3b8; font-size: 13px; cursor: pointer; transition: all 0.2s;
      font-family: inherit;
    }
    .suggestion:hover { background: rgba(59, 130, 246, 0.08); border-color: rgba(59, 130, 246, 0.2); color: #e2e8f0; }
    .suggestion mat-icon { font-size: 16px; width: 16px; height: 16px; }

    /* Messages */
    .message { display: flex; gap: 10px; animation: fadeInUp 0.3s ease-out; }
    .message.user { justify-content: flex-end; }
    .msg-avatar {
      width: 32px; height: 32px; border-radius: 10px; flex-shrink: 0;
      background: linear-gradient(135deg, #8b5cf6, #3b82f6);
      display: flex; align-items: center; justify-content: center;
    }
    .msg-avatar mat-icon { color: white; font-size: 16px; width: 16px; height: 16px; }
    .user-avatar { background: linear-gradient(135deg, #06b6d4, #3b82f6); }

    .msg-bubble {
      max-width: 70%; padding: 12px 16px; border-radius: 16px;
    }
    .message.assistant .msg-bubble {
      background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.06);
      border-top-left-radius: 4px;
    }
    .message.user .msg-bubble {
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      border-top-right-radius: 4px;
    }
    .msg-content { font-size: 14px; line-height: 1.6; white-space: pre-wrap; }
    .msg-sources {
      display: flex; align-items: center; gap: 4px; margin-top: 8px;
      padding-top: 8px; border-top: 1px solid rgba(255,255,255,0.06);
      font-size: 11px; color: #64748b;
    }
    .msg-sources mat-icon { font-size: 14px; width: 14px; height: 14px; }
    .msg-time { font-size: 10px; color: rgba(255,255,255,0.3); margin-top: 6px; }
    .message.user .msg-time { text-align: right; }

    .typing-bubble { display: flex; align-items: center; padding: 16px 20px; }
    .typing-dots { display: flex; gap: 4px; }
    .typing-dots span {
      width: 8px; height: 8px; border-radius: 50%;
      background: #64748b; animation: typingBounce 1.4s ease-in-out infinite;
    }
    .typing-dots span:nth-child(2) { animation-delay: 0.2s; }
    .typing-dots span:nth-child(3) { animation-delay: 0.4s; }
    @keyframes typingBounce {
      0%, 60%, 100% { transform: translateY(0); }
      30% { transform: translateY(-8px); }
    }

    /* Input */
    .input-area { padding: 16px 24px 20px; border-top: 1px solid rgba(255,255,255,0.06); }
    .input-box {
      display: flex; align-items: center; gap: 8px;
      padding: 4px 4px 4px 16px; border-radius: 16px;
      background: rgba(255,255,255,0.04);
      border: 1px solid rgba(255,255,255,0.08);
      transition: all 0.2s;
    }
    .input-box:focus-within { border-color: rgba(139, 92, 246, 0.3); box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.08); }
    .input-box input {
      flex: 1; background: none; border: none; outline: none;
      color: #f1f5f9; font-size: 14px; font-family: inherit; padding: 8px 0;
    }
    .input-box input::placeholder { color: #475569; }
    .send-btn {
      width: 36px; height: 36px; border-radius: 10px; border: none;
      background: #334155; color: #64748b; cursor: pointer;
      display: flex; align-items: center; justify-content: center;
      transition: all 0.2s;
    }
    .send-btn mat-icon { font-size: 18px; width: 18px; height: 18px; }
    .send-btn.active {
      background: linear-gradient(135deg, #8b5cf6, #3b82f6);
      color: white; box-shadow: 0 2px 10px rgba(139, 92, 246, 0.3);
    }
    .send-btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .disclaimer { text-align: center; font-size: 11px; color: #334155; margin-top: 8px; }

    @keyframes fadeInUp { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
  `]
})
export class AiChatComponent {
  @ViewChild('messagesArea') messagesArea!: ElementRef;
  messages: ChatMessage[] = [];
  userMessage = '';
  loading = false;
  sessionId: string | undefined;

  suggestions = [
    { icon: 'summarize', text: 'Summarize recent claims' },
    { icon: 'gpp_bad', text: 'Show fraud analysis' },
    { icon: 'policy', text: 'Explain coverage options' },
    { icon: 'help_outline', text: 'How does claim processing work?' },
  ];

  constructor(private aiService: AiService) {}

  useSuggestion(s: { text: string }): void {
    this.userMessage = s.text;
    this.sendMessage();
  }

  sendMessage(): void {
    const message = this.userMessage.trim();
    if (!message || this.loading) return;

    this.messages.push({ role: 'user', content: message, timestamp: new Date() });
    this.userMessage = '';
    this.loading = true;
    this.scrollToBottom();

    this.aiService.chat({ message, sessionId: this.sessionId }).subscribe({
      next: (response) => {
        this.loading = false;
        this.sessionId = response.data.sessionId;
        this.messages.push({
          role: 'assistant',
          content: response.data.response,
          sourceDocs: response.data.sourceDocs,
          timestamp: new Date()
        });
        this.scrollToBottom();
      },
      error: () => {
        this.loading = false;
        this.messages.push({
          role: 'assistant',
          content: 'Sorry, I encountered an error. Please try again.',
          timestamp: new Date()
        });
        this.scrollToBottom();
      }
    });
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      if (this.messagesArea) {
        this.messagesArea.nativeElement.scrollTop = this.messagesArea.nativeElement.scrollHeight;
      }
    }, 50);
  }
}
