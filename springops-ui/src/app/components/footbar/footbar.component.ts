import { Component, OnInit } from '@angular/core';
import { InfoService } from '../../services/info.service';
import {InfoDto} from "../../models/info.model";

@Component({
  selector: 'app-footbar',
  templateUrl: './footbar.component.html',
  styleUrls: ['./footbar.component.scss']
})
export class FootbarComponent implements OnInit {
  info?: InfoDto;

  constructor(private infoService: InfoService) {}

  ngOnInit(): void {
    this.infoService.getInfo().subscribe(data => {
      this.info = data;
    });
  }
}
