import { Component, OnInit, ViewChild } from '@angular/core';
import { WikiService } from "../../services/wiki.service";
import { MatSelectionList } from '@angular/material/list';

@Component({
  selector: 'app-wiki-viewer',
  templateUrl: './wiki-viewer.component.html',
  styleUrls: ['./wiki-viewer.component.scss']
})
export class WikiViewerComponent implements OnInit {
  @ViewChild('fileList') fileList: MatSelectionList | undefined;

  files: string[] = [];
  filteredFiles: string[] = [];
  selectedFile: string = '';
  markdown: string = '';
  isLoading = false;
  errorMessage = '';

  constructor(private wikiService: WikiService) {}

  ngOnInit(): void {
    this.loadFileList();
  }

  loadFileList(): void {
    this.isLoading = true;
    this.wikiService.getFileList().subscribe({
      next: files => {
        this.files = files;
        this.filteredFiles = [...files];
        this.isLoading = false;
      },
      error: err => {
        this.errorMessage = 'Failed to load file list';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  loadFile(name: string): void {
    if (this.selectedFile === name) return;

    this.isLoading = true;
    this.selectedFile = name;
    this.errorMessage = '';

    this.wikiService.getFileContent(name).subscribe({
      next: content => {
        this.markdown = content;
        this.isLoading = false;
      },
      error: err => {
        this.errorMessage = `Failed to load ${name}`;
        this.markdown = '';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  filterFiles(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value.toLowerCase();
    this.filteredFiles = this.files.filter(file =>
      file.toLowerCase().includes(filterValue)
    );
  }

  refreshContent(): void {
    if (this.selectedFile) {
      this.loadFile(this.selectedFile);
    }
  }

  downloadFile(): void {
    if (!this.selectedFile) return;

    const blob = new Blob([this.markdown], { type: 'text/markdown' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = this.selectedFile;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }

  printFile(): void {
    window.print();
  }

  getFileDate(file: string): Date {
    // Implement actual file date retrieval from your service if available
    return new Date(); // Placeholder
  }
}
