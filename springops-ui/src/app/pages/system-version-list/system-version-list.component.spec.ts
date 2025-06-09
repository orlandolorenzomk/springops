import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SystemVersionListComponent } from './system-version-list.component';

describe('SystemVersionListComponent', () => {
  let component: SystemVersionListComponent;
  let fixture: ComponentFixture<SystemVersionListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SystemVersionListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SystemVersionListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
