# -*- coding: utf-8 -*-
#
# Monitor documentation build configuration file.
#

import sys, os

# -- General configuration -----------------------------------------------------

sys.path.append(os.path.abspath('../_sphinx/exts'))
extensions = ['sphinx.ext.todo', 'includecode']

templates_path = ['_templates']
source_suffix = '.rst'
master_doc = 'index'
exclude_patterns = ['_build', 'pending', 'disabled']

project = u'Monitor'
copyright = u'Original contributors'
version = '@version@'
release = '@version@'

pygments_style = 'friendly'
highlight_language = 'scala'
add_function_parentheses = False
show_authors = True

# -- Options for HTML output ---------------------------------------------------

#html_theme = 'eigengo'
#html_theme_path = ['../_sphinx/themes']
#html_favicon = '../_sphinx/static/favicon.ico'

#html_title = 'Monitor Documentation'
#html_logo = '../_sphinx/static/logo.png'
#html_favicon = None

#html_static_path = ['../_sphinx/static']

#html_last_updated_fmt = '%b %d, %Y'
#html_domain_indices = False
#html_use_index = False
#html_show_sourcelink = False
#html_show_sphinx = False
#html_show_copyright = True
#htmlhelp_basename = 'monitordoc'
#html_use_smartypants = False
#html_add_permalinks = ''

#html_context = {
#  'include_analytics': 'online' in tags
#}

# -- Options for EPUB output ---------------------------------------------------
epub_author = "Original authors"
epub_language = "en"
epub_publisher = epub_author
epub_identifier = "http://github.com/eigengo/monitor/"
epub_scheme = "URL"
epub_cover = ("../_sphinx/static/logo.jpeg", "")

# -- Options for LaTeX output --------------------------------------------------

def setup(app):
  from sphinx.util.texescape import tex_replacements
  tex_replacements.append((u'⇒', ur'\(\Rightarrow\)'))

latex_paper_size = 'a4'
latex_font_size = '10pt'

latex_documents = [
  ('index', 'monitor.tex', u' Monitor Documentation',
   u'Original authors', 'manual')
]

latex_preamble = "\\definecolor{VerbatimColor}{rgb}{0.935,0.935,0.935}"

latex_elements = {
    'classoptions': ',oneside,openany',
    'babel': '\\usepackage[english]{babel}',
    'fontpkg': '\\PassOptionsToPackage{warn}{textcomp} \\usepackage{times}',
    'preamble': latex_preamble
    }

# latex_logo = '_sphinx/static/logo.jpeg'
