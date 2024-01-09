import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { catchError, Observable, switchMap, throwError } from 'rxjs';
import {AuthenticationService} from "../app/service/authentication.service";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthenticationService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    if(localStorage.getItem('accessToken')){
      const newRequest = req.clone({
        headers: req.headers.set('Authorization', `Bearer ${localStorage.getItem('accessToken')}`)
      })

      return next.handle(newRequest).pipe(
        catchError(err  => {
          if(err.status === 403){
            return this.handleRefresh(newRequest, next)
          } else {
            return throwError(()=> {
              new Error('Oups')
            })
          }
        })
      )
    }

    return next.handle(req)
  }

  handleRefresh(req: HttpRequest<any>, next: HttpHandler): Observable<any>{
    return this.authService.refresh().pipe(
      switchMap(tokenData => {
        const newRequest = req.clone({
          headers: req.headers.set('Authorization', `Bearer ${tokenData.accessToken}`)
        })
        return next.handle(newRequest)
      })
    )
  }
}
