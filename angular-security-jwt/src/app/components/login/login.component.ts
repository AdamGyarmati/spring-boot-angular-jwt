import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import {AuthenticationService} from "../../service/authentication.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  userEmail: string | null = null;
  authServiceLogin?: Subscription;
  authServiceForgotPassword?: Subscription;

  constructor(
    private authService: AuthenticationService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loginForm = new FormGroup({
      username: new FormControl('', [Validators.required]),
      password: new FormControl('', [Validators.required]),
    });
  }

  login() {
    const credential = this.loginForm.value;
    this.authServiceLogin = this.authService.login(credential).subscribe({
      next: (user) => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        console.log(err.error);
      },
    });
  }

  register() {
    if (this.loginForm.valid) {
      this.authService.signUp(this.loginForm.value).subscribe({
        error: (err) => {
          console.log(err.error);
        }
      });
    }
  }

  logout() {
    this.authService.logout().subscribe();
  }

  getMe() {
    this.authService.getMe().subscribe({
      next: (resp) => {
        console.log(resp);
      }
    })
  }

  refresh() {
    this.authService.refresh().subscribe();
  }

}
