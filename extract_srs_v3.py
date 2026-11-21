from docx import Document

doc = Document('DXN_Training_App_SRS-v3.0-Merged.docx')

with open('srs_v3_clean.md', 'w', encoding='utf-8') as f:
    for para in doc.paragraphs:
        text = para.text.strip()
        if text:
            f.write(text + '\n\n')
    
    for table in doc.tables:
        f.write('\n--- TABLE ---\n')
        for row in table.rows:
            cells = [cell.text.strip().replace('\n', ' | ') for cell in row.cells]
            f.write(' | '.join(cells) + '\n')
        f.write('--- END TABLE ---\n\n')

print('Extracted to srs_v3_clean.md')
