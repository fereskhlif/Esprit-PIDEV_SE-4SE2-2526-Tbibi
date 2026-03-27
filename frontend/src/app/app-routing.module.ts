import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { HealthGoalComponent } from './health-goal/health-goal.component';
import { MedicalChatComponent } from './medical-chat/medical-chat.component';
import { UsersComponent } from './users/users.component';

const routes: Routes = [
  { path: 'users', component: UsersComponent },
  { path: 'chat', component: MedicalChatComponent },
  { path: 'goals', component: HealthGoalComponent },
  { path: '', redirectTo: 'users', pathMatch: 'full' },
  { path: '**', redirectTo: 'users' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
