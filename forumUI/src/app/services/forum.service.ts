import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

const HttpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
};

@Injectable({
  providedIn: 'root'
})
export class ForumService {
  url: string;
  constructor(private http: HttpClient) { }

  postQuestion(question: any): Observable<any> {
    this.url = "//localhost:8080/forum/questions";
    return this.http.post(this.url, question, HttpOptions);
  }

  getDiscussionsSearchString(urlParameter: string): Observable<any> {
    console.log('Inside forum service');
    const searchStringUrl = "//localhost:8080/forum/questions?searchString=";
    return this.http.get<any[]>(searchStringUrl + urlParameter, HttpOptions);
  }

  getDiscussionsCategory(urlParameter: string): Observable<any> {
    console.log('Inside forum service');
    const searchStringUrl = "//localhost:8080/forum/questions?category=";
    return this.http.get<any[]>(searchStringUrl + urlParameter, HttpOptions);
  }


  getDiscussionsByUserId(urlParameter: string): Observable<any> {
    this.url = "//localhost:8080/forum/questions?userId=";
    return this.http.get<any>(this.url + urlParameter);
  }

  deleteQuestion(questionId: string): Observable<any> {
    this.url = "//localhost:8080/forum/questions/";
    this.url = this.url.concat(questionId.toString());
    return this.http.delete(this.url, HttpOptions);
  }

  closeQuestion(questionId: string): Observable<any> {
    this.url = "//localhost:8080/forum/questions/close-thread/";
    this.url = this.url.concat(questionId);
    return this.http.put(this.url, HttpOptions);
  }

  updateQuestion(questionId: string, question: string): Observable<any> {
    let params = new HttpParams().set("questionId", questionId);
    const updateQuestionHttpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
      params: params,
    };
    this.url = "//localhost:8080/forum/questions/";
    this.url = this.url.concat(questionId.toString());
    return this.http.put(this.url, question, updateQuestionHttpOptions);
  }

  postAnswer(answer:any): Observable<any> {
    this.url = "//localhost:8080/forum/answers";
    return this.http.post(this.url, answer, HttpOptions);
  }

  getAnswersByUserId(urlParameter: string): Observable<any> {
    this.url = "//localhost:8080/forum/answers?userId=";
    return this.http.get<any>(this.url + urlParameter);
  }

  deleteAnswer(answerId: number): Observable<any> {
    this.url = "//localhost:8080/forum/answers/";
    this.url = this.url.concat(answerId.toString());
    return this.http.delete(this.url, HttpOptions);
  }

  updateAnswer(answerId: number, answer: any): Observable<any> {
    let params = new HttpParams().set("answerId", answerId.toString());
    const updateAnswerHttpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
      params: params,
    };
    this.url = "//localhost:8080/forum/answers/";
    this.url = this.url.concat(answerId.toString());
    console.log(this.url);
    return this.http.put(this.url, answer, updateAnswerHttpOptions);
  }
}
