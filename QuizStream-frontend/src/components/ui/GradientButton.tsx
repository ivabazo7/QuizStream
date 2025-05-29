import { Button, ConfigProvider, Space } from 'antd';
import { createStyles } from 'antd-style';
import { CSSProperties } from 'react';

const useStyle = createStyles(({ prefixCls, css }) => ({
  linearGradientButton: css`
    &.${prefixCls}-btn-primary:not([disabled]):not(.${prefixCls}-btn-dangerous) {
      > span {
        position: relative;
      }

      &::before {
        content: '';
        background: linear-gradient(135deg, #6253e1, #04befe);
        position: absolute;
        inset: -1px;
        opacity: 1;
        transition: all 0.3s;
        border-radius: inherit;
      }

      &:hover::before {
        opacity: 0;
      }
    }
  `,
}));

type GradienButtonProps = {
  style?: CSSProperties | undefined;
  text: string;
  loading?: boolean;
  disabled?: boolean;
  onClick: React.MouseEventHandler<HTMLElement>;
};

function GradientButton({ style, text, loading, disabled, onClick }: GradienButtonProps) {
  const { styles } = useStyle();

  return (
    <ConfigProvider
      button={{
        className: styles.linearGradientButton,
      }}
    >
      <Space style={{ lineHeight: '60px' }}>
        <Button
          onClick={onClick}
          style={style}
          type="primary"
          size="large"
          loading={loading}
          disabled={disabled}
        >
          {text}
        </Button>
      </Space>
    </ConfigProvider>
  );
}

export default GradientButton;
