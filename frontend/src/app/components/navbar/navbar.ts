import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class Navbar {

  menuOpen: boolean = false;
  showLogoutDialog: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  goToProfile(): void {
    this.menuOpen = false;
    // später: this.router.navigate(['/profile']);
  }

  openLogoutDialog(): void {
    this.menuOpen = false;
    this.showLogoutDialog = true;
  }

  cancelLogout(): void {
    this.showLogoutDialog = false;
  }

  confirmLogout(): void {
    this.showLogoutDialog = false;
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
